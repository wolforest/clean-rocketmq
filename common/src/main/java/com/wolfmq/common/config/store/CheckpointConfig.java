package com.wolfmq.common.config.store;

import java.io.Serializable;
import lombok.Data;

@Data
public class CheckpointConfig implements Serializable {
    private int flushInterval = 1000;
}
