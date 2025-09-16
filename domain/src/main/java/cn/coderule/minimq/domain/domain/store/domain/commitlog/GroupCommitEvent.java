package cn.coderule.minimq.domain.domain.store.domain.commitlog;

import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import lombok.Data;

@Data
public class GroupCommitEvent implements Serializable {
    private EnqueueFuture enqueueFuture;

    private final long nextOffset;
    /**
     * Indicate the GroupCommitRequest result: true or false
     */
    private final CompletableFuture<EnqueueStatus> syncFuture = new CompletableFuture<>();
    /**
     * slave nums, in controller mode: -1
     */
    private volatile int ackNums = 1;

    private final long deadLine;

    public GroupCommitEvent(EnqueueFuture enqueueFuture, long timeoutMillis) {
        InsertResult insertResult = enqueueFuture.getInsertResult();
        this.nextOffset = insertResult.getWroteOffset() + insertResult.getWroteBytes();
        this.enqueueFuture = enqueueFuture;
        this.deadLine = System.nanoTime() + timeoutMillis * 1_000_000;
    }

    public GroupCommitEvent(long nextOffset, long timeoutMillis) {
        this.nextOffset = nextOffset;
        this.deadLine = System.nanoTime() + (timeoutMillis * 1_000_000);
    }

    public GroupCommitEvent(long nextOffset, long timeoutMillis, int ackNums) {
        this(nextOffset, timeoutMillis);
        this.ackNums = ackNums;
    }

    public void wakeupCustomer(final EnqueueStatus status) {
        this.syncFuture.complete(status);
    }

    public CompletableFuture<EnqueueStatus> future() {
        return syncFuture;
    }
}
