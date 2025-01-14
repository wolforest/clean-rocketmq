package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.enums.EnqueueStatus;
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
public class FlushResult implements Serializable {
    @Builder.Default
    private InsertResult insertResult = null;
    @Builder.Default
    private CompletableFuture<EnqueueResult> flushFuture = null;

    public FlushResult(InsertResult insertResult) {
        this.insertResult = insertResult;
    }

    public boolean isInsertSuccess() {
        if (insertResult == null) {
            return false;
        }

        return insertResult.isSuccess();
    }

    public static FlushResult success(InsertResult insertResult) {
        return FlushResult.builder()
            .insertResult(insertResult)
            .flushFuture(CompletableFuture.completedFuture(EnqueueResult.success(insertResult)))
            .build();
    }

    public static FlushResult failure() {
        return failure(EnqueueStatus.UNKNOWN_ERROR);
    }
    public static FlushResult failure(EnqueueStatus status) {
        return FlushResult.builder()
            .flushFuture(CompletableFuture.completedFuture(EnqueueResult.failure(status)))
            .build();
    }
}
