package cn.coderule.minimq.store.server.ha.commitlog;

import cn.coderule.minimq.domain.domain.model.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.cluster.store.InsertFuture;
import java.util.concurrent.CompletableFuture;

public class CommitLogSynchronizer {
    public CompletableFuture<EnqueueResult> sync(InsertFuture result) {
        return result.getFuture();
    }
}
