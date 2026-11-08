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
    protected Integer rpcPort;
    protected Integer grpcPort;
    protected Integer amqpPort;

    protected Integer storePort;
    protected Integer haPort;

    protected String flushType;

    protected Boolean enableCommitLogSharding;
    protected Integer commitLogShardingNum;
    protected Boolean enableCommitLogCache;

    private Boolean enableRocksDB;

    protected Boolean enableDisruptor;

    protected Integer mmapFileSize;
}
