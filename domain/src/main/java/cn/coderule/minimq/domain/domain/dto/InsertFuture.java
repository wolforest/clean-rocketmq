package cn.coderule.minimq.domain.domain.dto;

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
public class InsertFuture implements Serializable {
    @Builder.Default
    private InsertResult insertResult = null;
    @Builder.Default
    private CompletableFuture<EnqueueResult> future = null;

    public InsertFuture(InsertResult insertResult) {
        this.insertResult = insertResult;
    }

    public boolean isInsertSuccess() {
        if (insertResult == null) {
            return false;
        }

        return insertResult.isSuccess();
    }

    public static InsertFuture success(InsertResult insertResult) {
        return InsertFuture.builder()
            .insertResult(insertResult)
            .future(CompletableFuture.completedFuture(EnqueueResult.success(insertResult)))
            .build();
    }

    public static InsertFuture failure() {
        return failure(EnqueueStatus.UNKNOWN_ERROR);
    }
    public static InsertFuture failure(EnqueueStatus status) {
        return InsertFuture.builder()
            .future(CompletableFuture.completedFuture(EnqueueResult.failure(status)))
            .build();
    }
}
