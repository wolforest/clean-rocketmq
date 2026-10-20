package cn.coderule.wolfmq.domain.config.base;

import java.io.Serializable;
import lombok.Data;

/**
 * Common attributes,
 *  - load from config file
 *  - will change ConfigAttribute
 */
@Data
public class Common implements Serializable {
    private Integer rpcPort;
    private Integer grpcPort;
    private Integer amqpPort;

    private Integer storePort;
    private Integer haPort;

    private String flushType;

    private Boolean enableCommitLogSharding;
    private Integer commitLogShardingNum;
    private Boolean enableCommitLogCache;

    private Boolean enableDisruptor;

    private Integer mmapFileSize;
}
