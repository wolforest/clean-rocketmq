package com.wolfmq.common.config.store;

import com.wolfmq.common.config.base.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class IndexConfig implements Serializable {
    private boolean enableRocksDB = ConfigAttribute.ENABLE_ROCKSDB;
    private boolean enableTagIndex = false;

    private int maxHashSlotNum = 500_000;
    private int maxIndexNum = 20_000_000;

    private int minFlushPages = 1;
    private int flushInterval = 1000 * 10;
}
