package com.wolfmq.common.config.base;

import java.io.Serializable;
import lombok.Data;

/**
 * Common attributes,
 *  - load from config file
 *  - will change ConfigAttribute
 */
@Data
public class Common implements Serializable {
    /* socket port attributes */
    protected Integer rpcPort;
    protected Integer grpcPort;
    protected Integer amqpPort;
    protected Integer storePort;
    protected Integer haPort;

    /* commitlog attributes */
    protected String flushType;
    protected Boolean enableCommitLogSharding;
    protected Integer commitLogShardingNum;
    protected Boolean enableCommitLogCache;

    /* special attributes */
    private Boolean enablePreAssignQueueOffset;
    private Boolean enableRocksDB;
    protected Boolean enableDisruptor;
    protected Integer mmapFileSize;
}
