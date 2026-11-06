package com.wolfmq.common.config.store;

import com.wolfmq.common.domain.retention.RetentionStrategy;
import java.io.Serializable;
import lombok.Data;

@Data
public class RetentionConfig implements Serializable {
    private RetentionStrategy strategy;
    private double maxCleanupPercentage = 0.30;
}
