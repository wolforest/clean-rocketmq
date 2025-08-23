package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.bean.ExceptionUtil;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.core.enums.consume.AckStatus;
import cn.coderule.minimq.domain.core.exception.BrokerException;
import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroup;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.domain.consumer.receipt.RenewStrategyPolicy;
import cn.coderule.minimq.domain.domain.consumer.revive.RenewEvent;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import cn.coderule.minimq.domain.service.broker.consume.RetryPolicy;
import com.google.common.base.Stopwatch;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RenewService implements Lifecycle {
    private static final RetryPolicy RENEW_POLICY = new RenewStrategyPolicy();

    private final MessageConfig messageConfig;

    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduler;

    private final ConsumerRegister consumerRegister;
    private final ReceiptHandler receiptHandler;

    private SubscriptionStore subscriptionStore;
    private RenewListener renewListener;

    public RenewService(
        BrokerConfig brokerConfig,
        ConsumerRegister consumerRegister,
        ReceiptHandler receiptHandler
    ) {
        this.messageConfig = brokerConfig.getMessageConfig();

        this.consumerRegister = consumerRegister;
        this.receiptHandler = receiptHandler;

        this.executor = initExecutor();
        this.scheduler = initScheduler();
    }

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
                renewByEntry(entry);
            }
        } catch (Exception e) {
            log.error("renew error in {}", this.getClass().getSimpleName(), e);
        }

        log.info("renew finished in {}, cost: {}ms",
            this.getClass().getSimpleName(), stopwatch.elapsed().toMillis());
    }



    private void renewByEntry(Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup> entry) {
        if (isClientOffline(entry.getKey())) {
            receiptHandler.removeGroup(entry.getKey());
            return;
        }

        ReceiptHandleGroupKey key = entry.getKey();
        ReceiptHandleGroup group = entry.getValue();

        entry.getValue().scan((msgID, handleStr, v) ->
            submitRenewTask(key, group, msgID, handleStr)
        );
    }

    private void submitRenewTask(ReceiptHandleGroupKey key, ReceiptHandleGroup group, String msgID, String handleStr) {
        long now = System.currentTimeMillis();
        ReceiptHandle handle = ReceiptHandle.decode(handleStr);

        if (handle.getNextVisibleTime() - now > messageConfig.getRenewAheadTime()) {
            return;
        }

        executor.submit(
            () -> renewByMsgID(key, group, msgID, handleStr)
        );
    }

    private void renewByMsgID(ReceiptHandleGroupKey key, ReceiptHandleGroup group, String msgID, String handleStr) {
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

    private CompletableFuture<MessageReceipt> renewMessage(ReceiptHandleGroupKey key, MessageReceipt receipt) {
        CompletableFuture<MessageReceipt> future = new CompletableFuture<>();
        long now = System.currentTimeMillis();

        try {
            if (receipt.getRenewRetryTimes() > messageConfig.getMaxRenewRetryTimes()) {
                log.warn("Renew message failed, retry times exceed max retry times, receipt={};", receipt);
                return CompletableFuture.completedFuture(null);
            }

            if (now - receipt.getConsumeTimestamp() < messageConfig.getMaxRenewTime()) {
                renewUnexpiredMessage(future, key, receipt);
            } else {
                processExpiredMessage(future, key, receipt);
            }
        } catch (Exception e) {
            log.error("renewMessage error, receipt: {}", receipt, e);
        }

        return future;
    }

    private void renewUnexpiredMessage(CompletableFuture<MessageReceipt> future, ReceiptHandleGroupKey key, MessageReceipt receipt) {
        CompletableFuture<AckResult> ackFuture = new CompletableFuture<>();

        long renewTime = RENEW_POLICY.nextDelayDuration(receipt.getRenewTimes());
        renewListener.fireRenewEvent(renewTime, receipt, key, ackFuture);

        ackFuture.whenComplete((ackResult, throwable) -> {
            renewCallback(future, ackResult, throwable, receipt);
        });
    }

    private void renewCallback(CompletableFuture<MessageReceipt> future, AckResult ackResult, Throwable t, MessageReceipt receipt) {
        if (null != t) {
            handleRenewException(t, receipt, future);
            return;
        }

        if (!ackResult.isSuccess()) {
            log.error("Renew failed, receipt: {}, result: {}", receipt, ackResult);
            future.complete(null);
            return;
        }

        receipt.updateReceiptHandle(ackResult.getExtraInfo());
        receipt.resetRenewRetryTimes();
        receipt.incrementRenewTimes();
        future.complete(receipt);
    }

    private void handleRenewException(
        Throwable t,
        MessageReceipt receipt,
        CompletableFuture<MessageReceipt> future
    ) {
        log.error("Renew message error, receipt: {}", receipt, t);
        if (!isRetryException(t)) {
            future.completeExceptionally(null);
            return;
        }

        receipt.incrementAndGetRenewRetryTimes();
        future.complete(receipt);
    }

    private boolean isRetryException(Throwable t) {
        t = ExceptionUtil.getRealException(t);

        if (!(t instanceof BrokerException brokerException)) {
            return true;
        }

        return !BrokerExceptionCode.INVALID_BROKER_NAME.equals(brokerException.getCode())
            && !BrokerExceptionCode.INVALID_RECEIPT_HANDLE.equals(brokerException.getCode());
    }

    private void processExpiredMessage(
        CompletableFuture<MessageReceipt> future,
        ReceiptHandleGroupKey key,
        MessageReceipt receipt
    ) {
        RequestContext context = RequestContext.createForInner(
            this.getClass().getSimpleName() + "RenewMessage"
        );
        SubscriptionGroup group = subscriptionStore.getGroup(
            receipt.getTopic(), receipt.getGroup()
        );
        if (group == null) {
            log.error("subscription group not found while renew, receipt={}", receipt);
            future.complete(null);
            return;
        }

        RetryPolicy retryPolicy = group.getGroupRetryPolicy().getRetryPolicy();
        long retryTime = retryPolicy.nextDelayDuration(receipt.getReconsumeTimes());
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

    private boolean isClientOffline(ReceiptHandleGroupKey key) {
        ClientChannelInfo channelInfo = consumerRegister.findChannel(
            key.getGroup(), key.getChannel()
        );

        return null == channelInfo;
    }
}
