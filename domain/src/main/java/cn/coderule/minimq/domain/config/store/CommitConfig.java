package cn.coderule.minimq.domain.config.store;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.domain.config.ConfigAttribute;
import cn.coderule.minimq.domain.core.enums.store.FlushType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CommitConfig implements Serializable {
    private boolean enableMultiDir = false;
    private boolean enableWriteCache = false;

    private boolean enableDisruptor = ConfigAttribute.ENABLE_DISRUPTOR;
    private int defaultOfferTimeout = 3_000;
    private int defaultPollTimeout = 30;

    private boolean enableSharding = false;
    private boolean bindShardingWithCpu = true;
    private int maxShardingNumber = 100;
    private int shardingNumber = SystemUtil.getProcessorNumber();


    private int dispatchThreads = 5;

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
    private int ThroughCommitInterval = 200;

}
