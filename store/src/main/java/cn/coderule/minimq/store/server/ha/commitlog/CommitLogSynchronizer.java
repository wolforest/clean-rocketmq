package cn.coderule.minimq.store.server.ha.commitlog;

import cn.coderule.minimq.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.dto.InsertFuture;
import java.util.concurrent.CompletableFuture;

public class CommitLogSynchronizer {
    public CompletableFuture<EnqueueResult> sync(InsertFuture result) {
        return result.getFuture();
    }
}
