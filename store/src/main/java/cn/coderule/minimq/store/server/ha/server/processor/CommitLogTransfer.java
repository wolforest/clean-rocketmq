package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogTransfer extends ServiceThread implements Lifecycle {

    @Override
    public String getServiceName() {
        return CommitLogTransfer.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    private void transfer() {

    }
}
