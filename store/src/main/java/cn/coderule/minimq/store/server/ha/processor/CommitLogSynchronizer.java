package cn.coderule.minimq.store.server.ha.processor;

import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import java.util.concurrent.CompletableFuture;

public class CommitLogSynchronizer {
    public CompletableFuture<EnqueueResult> sync(InsertFuture result) {
        return result.getFuture();
    }
}
