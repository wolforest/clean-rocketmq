package com.wolfmq.store.domain.retention.cleanup;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CleanContext implements Serializable {
    private LocalDateTime startTime;
}
