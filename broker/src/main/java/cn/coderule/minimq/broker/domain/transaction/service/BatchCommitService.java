package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;

public class BatchCommitService extends ServiceThread {
    private final CommitBuffer commitBuffer;
    private final MessageService messageService;

    public BatchCommitService(CommitBuffer commitBuffer, MessageService messageService) {
        this.commitBuffer = commitBuffer;
        this.messageService = messageService;
    }

    @Override
    public String getServiceName() {
        return BatchCommitService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
