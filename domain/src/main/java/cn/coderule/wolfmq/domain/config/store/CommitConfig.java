package cn.coderule.wolfmq.domain.config.store;

import cn.coderule.wolfmq.domain.config.base.ConfigAttribute;
import cn.coderule.wolfmq.domain.core.enums.store.FlushType;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitConfig implements Serializable {
    private boolean enableMultiDir = false;
    private boolean enableWriteCache = ConfigAttribute.ENABLE_COMMITLOG_CACHE;

    private boolean enableDisruptor = ConfigAttribute.ENABLE_DISRUPTOR;
    private int defaultOfferTimeout = 3_000;
    private int defaultPollTimeout = 30;

    private boolean enableSharding = ConfigAttribute.ENABLE_COMMITLOG_SHARDING;
    private boolean bindShardingWithCpu = true;
    private int maxShardingNumber = 100;
    private int shardingNumber = ConfigAttribute.COMMITLOG_SHARDING_NUM;


    private int dispatchThreads = 5;
    private int dispatchInterval = 10;

    private String dirName = "commitlog";

    private int fileSize = ConfigAttribute.MMAP_FILE_SIZE;

    private boolean enableFlushSleep = true;
    private FlushType flushType = ConfigAttribute.FLUSH_TYPE;
    private int minFlushPages = 4;
    private int flushInterval = 500;
    private int flushTimeout = 5_000;
    private int ThroughFlushInterval = 10_000;

    private int minCommitPages = 4;

    private int commitInterval = 200;
    private int throughCommitInterval = 200;

}
