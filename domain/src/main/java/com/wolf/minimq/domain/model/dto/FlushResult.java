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
    private EnqueueStatus status;
    @Builder.Default
    private InsertResult insertResult = null;
    @Builder.Default
    private CompletableFuture<EnqueueResult> flushFuture = null;

    public FlushResult(EnqueueStatus status, InsertResult insertResult) {
        this.status = status;
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
            .status(EnqueueStatus.PUT_OK)
            .insertResult(insertResult)
            .build();
    }

    public static FlushResult failure() {
        return failure(EnqueueStatus.UNKNOWN_ERROR);
    }
    public static FlushResult failure(EnqueueStatus status) {
        return FlushResult.builder()
            .status(status)
            .build();
    }
}
