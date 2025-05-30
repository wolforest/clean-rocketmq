package cn.coderule.minimq.store.domain.commitlog.vo;

import cn.coderule.minimq.domain.domain.enums.store.EnqueueStatus;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCommitRequest implements Serializable {
    private long offset;
    private long nextOffset;
    /**
     * Indicate the GroupCommitRequest result: true or false
     */
    @Builder.Default
    private CompletableFuture<EnqueueStatus> flushOKFuture = new CompletableFuture<>();
    /**
     * slave nums, in controller mode: -1
     */
    @Builder.Default
    private volatile int ackNums = 1;

    private long deadLine;

    public void wakeup(EnqueueStatus status) {
        this.flushOKFuture.complete(status);
    }

    public CompletableFuture<EnqueueStatus> future() {
        return flushOKFuture;
    }
}
