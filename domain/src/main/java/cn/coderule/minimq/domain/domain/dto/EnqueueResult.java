package cn.coderule.minimq.domain.domain.dto;

import cn.coderule.minimq.domain.domain.enums.store.EnqueueStatus;
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
    private String regionId;

    private long queueOffset;
    private boolean enableTrace = true;

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

    public static EnqueueResult success(InsertResult insertResult) {
        return new EnqueueResult(EnqueueStatus.PUT_OK, insertResult);
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
