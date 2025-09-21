package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;

public class BatchCommitService extends ServiceThread {
    private final CommitBuffer commitBuffer;

    public BatchCommitService(CommitBuffer commitBuffer) {
        this.commitBuffer = commitBuffer;
    }

    @Override
    public String getServiceName() {
        return BatchCommitService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
