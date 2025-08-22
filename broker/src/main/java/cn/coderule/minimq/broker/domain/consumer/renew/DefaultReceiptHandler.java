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
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroup;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.domain.consumer.receipt.RenewStrategyPolicy;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import cn.coderule.minimq.domain.service.broker.consume.RetryPolicy;
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
    private ConsumeHookManager hookManager;
    private SubscriptionStore subscriptionStore;
    private ConsumerRegister consumerRegister;
    private RenewListener renewListener;

    private ConcurrentMap<ReceiptHandleGroupKey, ReceiptHandleGroup> groupMap
        = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;
    private ThreadPoolExecutor renewExecutor;

    private ScheduledExecutorService initScheduler() {
        return ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("RenewSchedulerThread_")
        );
    }

    private ThreadPoolExecutor initExecutor() {
        MessageConfig messageConfig = brokerConfig.getMessageConfig();
        return ThreadPoolFactory.create(
            messageConfig.getMinRenewThreadNum(),
            messageConfig.getMaxRenewThreadNum(),
            1,
            TimeUnit.MINUTES,
            "RenewWorkerThread",
             messageConfig.getRenewQueueCapacity()
        );
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void addReceipt(RequestContext context, MessageReceipt messageReceipt) {

    }

    @Override
    public MessageReceipt removeReceipt(RequestContext context, MessageReceipt messageReceipt) {
        return null;
    }

    @Override
    public void clearGroup(ReceiptHandleGroupKey key) {

    }
}
