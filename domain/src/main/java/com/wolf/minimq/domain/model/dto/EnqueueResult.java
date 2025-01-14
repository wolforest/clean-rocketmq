package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.enums.FlushStatus;
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
    private FlushStatus status;
    private InsertResult insertResult;

    public EnqueueResult(FlushStatus status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return FlushStatus.PUT_OK.equals(status);
    }

    public static EnqueueResult success(InsertResult insertResult) {
        return new EnqueueResult(FlushStatus.PUT_OK, insertResult);
    }

    public static EnqueueResult failure() {
        return new EnqueueResult(FlushStatus.UNKNOWN_ERROR);
    }
}
