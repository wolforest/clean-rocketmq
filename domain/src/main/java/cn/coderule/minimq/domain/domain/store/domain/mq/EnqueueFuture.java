package cn.coderule.minimq.domain.domain.store.domain.mq;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnqueueFuture implements Serializable {
    @Builder.Default
    private InsertResult insertResult = null;
    @Builder.Default
    private CompletableFuture<EnqueueResult> future = null;

    public EnqueueFuture(InsertResult insertResult) {
        this.insertResult = insertResult;
    }

    public EnqueueResult get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    public boolean isInsertSuccess() {
        if (insertResult == null) {
            return false;
        }

        return insertResult.isSuccess();
    }

    public static EnqueueFuture success(InsertResult insertResult, MessageBO messageBO) {
        EnqueueResult result = EnqueueResult.success(insertResult, messageBO);
        return EnqueueFuture.builder()
            .insertResult(insertResult)
            .future(CompletableFuture.completedFuture(result))
            .build();
    }

    public static EnqueueFuture failure() {
        return failure(EnqueueStatus.UNKNOWN_ERROR);
    }
    public static EnqueueFuture failure(EnqueueStatus status) {
        return EnqueueFuture.builder()
            .future(CompletableFuture.completedFuture(EnqueueResult.failure(status)))
            .build();
    }
}
