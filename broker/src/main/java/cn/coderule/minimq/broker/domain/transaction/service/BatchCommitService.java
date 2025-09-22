package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;

public class BatchCommitService extends ServiceThread {
    private final TransactionConfig transactionConfig;
    private final CommitBuffer commitBuffer;
    private final MQStore mqStore;
    private final MessageFactory messageFactory;

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
    }

    @Override
    public String getServiceName() {
        return BatchCommitService.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    private void commit() {

    }
}
