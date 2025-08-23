package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroup;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import com.google.common.base.Stopwatch;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RenewService implements Lifecycle {
    private BrokerConfig brokerConfig;
    private MessageConfig messageConfig;

    private ThreadPoolExecutor executor;
    private ScheduledExecutorService scheduler;

    private ConsumerRegister consumerRegister;
    private ReceiptHandler receiptHandler;

    @Override
    public void start() throws Exception {
        startScheduler();
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
        scheduler.shutdown();
    }

    private ScheduledExecutorService initScheduler() {
        return ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("RenewSchedulerThread_")
        );
    }

    private void startScheduler() {
        scheduler.scheduleWithFixedDelay(
            this::renew,
            0,
            messageConfig.getRenewInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    private void renew() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            for (Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup> entry : receiptHandler.getEntrySet()) {
                renew(entry);
            }
        } catch (Exception e) {
            log.error("renew error in {}", this.getClass().getSimpleName(), e);
        }

        log.info("renew finished in {}, cost: {}ms",
            this.getClass().getSimpleName(), stopwatch.elapsed().toMillis());
    }

    private boolean isClientOffline(ReceiptHandleGroupKey key) {
        ClientChannelInfo channelInfo = consumerRegister.findChannel(
            key.getGroup(), key.getChannel()
        );

        return null == channelInfo;
    }

    private void renew(Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup> entry) {
        if (isClientOffline(entry.getKey())) {
            receiptHandler.removeGroup(entry.getKey());
            return;
        }

        ReceiptHandleGroupKey key = entry.getKey();
        ReceiptHandleGroup group = entry.getValue();

        entry.getValue().scan((msgID, handleStr, v) ->
            renew(key, group, msgID, handleStr)
        );
    }

    private void renew(ReceiptHandleGroupKey key, ReceiptHandleGroup group, String msgID, String handleStr) {
        long now = System.currentTimeMillis();
        ReceiptHandle handle = ReceiptHandle.decode(handleStr);

        if (handle.getNextVisibleTime() - now > messageConfig.getRenewAheadTime()) {
            return;
        }

        executor.submit(
            () -> renewMessage(key, group, msgID, handleStr)
        );
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
}
