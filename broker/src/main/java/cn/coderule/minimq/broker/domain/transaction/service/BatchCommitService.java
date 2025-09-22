package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchCommitService extends ServiceThread {
    private final TransactionConfig transactionConfig;
    private final CommitBuffer commitBuffer;
    private final MQStore mqStore;
    private final MessageFactory messageFactory;

    private long wakeupTime;

    public BatchCommitService(
        TransactionConfig transactionConfig,
        CommitBuffer commitBuffer,
        MessageFactory messageFactory,
        MQStore mqStore
    ) {
        this.transactionConfig = transactionConfig;

        this.commitBuffer = commitBuffer;
        this.messageFactory = messageFactory;

        this.mqStore = mqStore;
        initWakeupTime();
    }

    @Override
    public String getServiceName() {
        return BatchCommitService.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            long interval = calculateCommitInterval();
            if (interval > 0) {
                this.await(interval);
            }

            commit();
        }

        log.info("{} service stopped", this.getServiceName());
    }

    private void initWakeupTime() {
        long now = System.currentTimeMillis();
        long interval = transactionConfig.getBatchCommitInterval();

        this.wakeupTime = now + interval;
    }

    private long calculateCommitInterval() {
        long now = System.currentTimeMillis();
        long interval = wakeupTime - now;
        if (interval <= 0) {
            interval = 0;
        }

        return interval;
    }

    private void commit() {

    }
}
