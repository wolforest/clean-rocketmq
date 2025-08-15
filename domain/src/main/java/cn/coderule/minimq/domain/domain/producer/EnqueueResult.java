package cn.coderule.minimq.domain.domain.producer;

import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnqueueResult implements Serializable {
    private EnqueueStatus status;
    private InsertResult insertResult;

    private String messageId;
    private String transactionId;
    private String region;

    private int queueId;
    private long queueOffset;
    private long commitOffset;
    private boolean enableTrace = false;

    public EnqueueResult(EnqueueStatus status) {
        this.status = status;
    }

    public EnqueueResult(EnqueueStatus status, InsertResult insertResult) {
        this.status = status;
        this.insertResult = insertResult;
    }

    public boolean isSuccess() {
        return EnqueueStatus.PUT_OK.equals(status);
    }

    public boolean isFailure() {
        return status != EnqueueStatus.PUT_OK
            && status != EnqueueStatus.FLUSH_DISK_TIMEOUT
            && status != EnqueueStatus.FLUSH_SLAVE_TIMEOUT
            && status != EnqueueStatus.SLAVE_NOT_AVAILABLE;
    }

    public static EnqueueResult success(InsertResult insertResult, MessageBO messageBO) {
        return EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .insertResult(insertResult)
            .messageId(messageBO.getMessageId())
            .transactionId(messageBO.getTransactionId())
            .commitOffset(messageBO.getCommitOffset())
            .queueOffset(messageBO.getQueueOffset())
            .queueId(messageBO.getQueueId())
            .build();
    }

    public static EnqueueResult failure() {
        return new EnqueueResult(EnqueueStatus.UNKNOWN_ERROR);
    }


    public static EnqueueResult notAvailable() {
        return new EnqueueResult(EnqueueStatus.SERVICE_NOT_AVAILABLE);
    }


    public static EnqueueResult failure(EnqueueStatus status) {
        return new EnqueueResult(status);
    }
}
