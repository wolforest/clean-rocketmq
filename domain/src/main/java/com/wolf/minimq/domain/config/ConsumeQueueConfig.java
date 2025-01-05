package com.wolf.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class ConsumeQueueConfig implements Serializable {
    private int unitSize = 20;
    // fileSize = unitNum * unitSize
    private int fileSize = 300_000 * 20;
}
