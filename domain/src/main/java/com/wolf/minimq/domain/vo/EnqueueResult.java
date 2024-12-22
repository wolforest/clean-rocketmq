package com.wolf.minimq.domain.vo;

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
    private AppendResult appendResult;

    public EnqueueResult(EnqueueStatus status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return EnqueueStatus.PUT_OK.equals(status);
    }
}
