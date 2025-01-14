package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.enums.EnqueueStatus;
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

    public EnqueueResult(EnqueueStatus status) {
        this.status = status;
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

    public static EnqueueResult failure(EnqueueStatus status) {
        return new EnqueueResult(status);
    }
}
