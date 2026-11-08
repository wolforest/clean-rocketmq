package com.wolfmq.common.config.store;

import com.wolfmq.common.config.base.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class MetaConfig implements Serializable {
   private boolean enableRocksDB = ConfigAttribute.ENABLE_ROCKSDB;

   private long consumeOffsetVersionUpdateStep = 500;
   private long delayOffsetVersionUpdateStep = 200;


   private boolean enableOffsetCheckInSlave = false;
}
