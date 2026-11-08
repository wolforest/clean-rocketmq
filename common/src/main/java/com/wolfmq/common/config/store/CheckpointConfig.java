package com.wolfmq.common.config.store;

import com.wolfmq.common.config.base.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class CheckpointConfig implements Serializable {
    private boolean enableRocksDB = ConfigAttribute.ENABLE_ROCKSDB;
    
    private int initialDelay = 5000;
    private int flushInterval = 1000;
}
