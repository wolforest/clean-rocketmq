package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogSynchronizer extends ServiceThread {

    @Override
    public String getServiceName() {
        return CommitLogSynchronizer.class.getSimpleName();
    }

    public CompletableFuture<EnqueueResult> sync(InsertFuture result) {
        return result.getFuture();
    }

    @Override
    public void run() {

    }
}
