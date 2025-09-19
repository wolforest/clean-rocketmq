package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.domain.message.MessageBO;
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
public class CommitResult implements Serializable {
    private int responseCode;
    private String responseMessage;

    private MessageBO messageBO;


    public static CommitResult success() {
        return CommitResult.builder()
            .responseCode(1)
            .build();
    }

    public static CommitResult failure() {
        return CommitResult.builder()
            .responseCode(-1)
            .build();
    }

    public static CompletableFuture<CommitResult> failureFuture() {
        return CompletableFuture.completedFuture(failure());
    }

}
