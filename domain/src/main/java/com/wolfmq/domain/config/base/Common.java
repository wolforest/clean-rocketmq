package com.wolfmq.domain.config.base;

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

    protected Boolean enableDisruptor;

    protected Integer mmapFileSize;
}
