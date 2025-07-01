package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.transaction.DeleteBuffer;

public class BatchCommitService extends ServiceThread {
    private final DeleteBuffer deleteBuffer;
    private final MessageService messageService;

    public BatchCommitService(DeleteBuffer deleteBuffer, MessageService messageService) {
        this.deleteBuffer = deleteBuffer;
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
