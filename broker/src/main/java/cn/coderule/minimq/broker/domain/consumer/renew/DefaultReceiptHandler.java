package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroup;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.domain.consumer.receipt.RenewStrategyPolicy;
import cn.coderule.minimq.domain.domain.consumer.revive.RenewEvent;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import cn.coderule.minimq.domain.service.broker.consume.RetryPolicy;
import com.google.common.base.Stopwatch;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultReceiptHandler implements ReceiptHandler, Lifecycle {
    private static final RetryPolicy RENEW_POLICY = new RenewStrategyPolicy();

    private BrokerConfig brokerConfig;
    private MessageConfig messageConfig;

    private ConsumeHookManager hookManager;
    private SubscriptionStore subscriptionStore;
    private ConsumerRegister consumerRegister;
    private RenewListener renewListener;

    private ConcurrentMap<ReceiptHandleGroupKey, ReceiptHandleGroup> groupMap
        = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;
    private ThreadPoolExecutor executor;

    @Override
    public void start() throws Exception {
        startScheduler();
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
        scheduler.shutdown();
        clearGroup();
    }

    @Override
    public void addReceipt(MessageReceipt messageReceipt) {
        ReceiptHandleGroupKey key = new ReceiptHandleGroupKey(
            messageReceipt.getChannel(),
            messageReceipt.getGroup()
        );

        ReceiptHandleGroup group = groupMap.computeIfAbsent(
            key, k -> new ReceiptHandleGroup(messageConfig)
        );
        group.put(messageReceipt.getMessageId(), messageReceipt);
    }

    @Override
    public MessageReceipt removeReceipt(MessageReceipt messageReceipt) {
        ReceiptHandleGroupKey key = new ReceiptHandleGroupKey(
            messageReceipt.getChannel(),
            messageReceipt.getGroup()
        );

        ReceiptHandleGroup group = groupMap.get(key);
        if (group == null) {
            return null;
        }
        return group.remove(messageReceipt.getMessageId(), messageReceipt.getReceiptHandleStr());
    }

    @Override
    public void removeGroup(ReceiptHandleGroupKey key) {
        if (key == null) {
            return;
        }

        ReceiptHandleGroup group = groupMap.remove(key);
        if (group == null) {
            return;
        }

        group.scan(((msgID, handle, v) -> {
            scanGroup(key, group, msgID, handle);
        }));
    }

    private void scanGroup(
        ReceiptHandleGroupKey key,
        ReceiptHandleGroup group,
        String msgID,
        String handle
    ) {
        try {
            group.computeIfPresent(msgID, handle, receipt -> {
                fireRenewEvent(key, receipt);
                return CompletableFuture.completedFuture(null);
            });
        } catch (Exception e) {
            log.error("clear handle group error, key={}", key, e);
        }
    }

    private void fireRenewEvent(
        ReceiptHandleGroupKey key,
        MessageReceipt receipt
    ) {
        RenewEvent event = RenewEvent.builder()
            .key(key)
            .messageReceipt(receipt)
            .future(new CompletableFuture<>())
            .eventType(RenewEvent.EventType.CLEAR_GROUP)
            .renewTime(messageConfig.getInvisibleTimeOfClear())
            .build();

        renewListener.fire(event);
    }

    private void clearGroup() {
        log.info("start clear receipt handle in {}", this.getClass().getSimpleName());

        for (ReceiptHandleGroupKey key : groupMap.keySet()) {
            removeGroup(key);
        }

        log.info("finish clear receipt handle in {}", this.getClass().getSimpleName());
    }

    private ScheduledExecutorService initScheduler() {
        return ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("RenewSchedulerThread_")
        );
    }

    private ThreadPoolExecutor initExecutor() {
        ThreadPoolExecutor executor = ThreadPoolFactory.create(
            messageConfig.getMinRenewThreadNum(),
            messageConfig.getMaxRenewThreadNum(),
            1,
            TimeUnit.MINUTES,
            "RenewWorkerThread",
            messageConfig.getRenewQueueCapacity()
        );

        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("add renew task failed. queueSize={}", executor.getQueue().size());
        });

        return executor;
    }

    private void startScheduler() {
        scheduler.scheduleWithFixedDelay(
            this::renew,
            0,
            messageConfig.getRenewInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    private boolean isClientOffline(ReceiptHandleGroupKey key) {
        ClientChannelInfo channelInfo = consumerRegister.findChannel(
            key.getGroup(), key.getChannel()
        );

        return null == channelInfo;
    }

    private void renew() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            for (Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup> entry : groupMap.entrySet()) {
                if (isClientOffline(entry.getKey())) {
                    removeGroup(entry.getKey());
                    continue;
                }


                entry.getValue().scan((msgID, handleStr, v) -> {
                    long now = System.currentTimeMillis();
                    ReceiptHandle handle = ReceiptHandle.decode(handleStr);

                    if (handle.getNextVisibleTime() - now > messageConfig.getRenewAheadTime()) {
                        return;
                    }

                    executor.submit(
                        () -> renewMessage(entry.getKey(), entry.getValue(), msgID, handleStr)
                    );
                });
            }
        } catch (Exception e) {
            log.error("renew error in {}", this.getClass().getSimpleName(), e);
        }

        log.info("renew finished in {}, cost: {}ms",
            this.getClass().getSimpleName(), stopwatch.elapsed().toMillis());
    }

    private void renewMessage(ReceiptHandleGroupKey key, ReceiptHandleGroup group, String msgID, String handleStr) {
        try {
            group.computeIfPresent(
                msgID,
                handleStr,
                messageReceipt -> renewMessage(key, messageReceipt)
            );
        } catch (Exception e) {
            log.error("renew error in {}, msgId={}; handleStr={};",
                this.getClass().getSimpleName(), msgID, handleStr, e);
        }
    }

    private CompletableFuture<MessageReceipt> renewMessage(ReceiptHandleGroupKey key, MessageReceipt messageReceipt) {
        return null;
    }

}
