package cn.coderule.minimq.domain.config.business;

import cn.coderule.minimq.domain.config.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class TimerConfig implements Serializable {
    private boolean enableTimer = true;
    private boolean enableRocksDB = false;
    private boolean enableWarmup = false;
    private boolean skipUnknownError = false;
    private boolean enableDisruptor = false;

    private boolean stopConsume = false;
    private boolean stopScan = false;

    private int consumeBatchNum = 32;
    private int consumeMaxNum = 32;

    private int timerLogFileSize = ConfigAttribute.MMAP_FILE_SIZE;
    /**
     * sharding timer task by file(commitLog)
     * it should be equal to commitLog file size
     */
    private int shardingFileSize = ConfigAttribute.MMAP_FILE_SIZE;

    private int totalSlots = 7 * 24 * 3600;
    private int wheelSlots = 2 * 24 * 3600;
    private int precision = 1_000;

    private int flushInterval = 1_000;
    private int consumerThreadNum = 3;
    private int producerThreadNum = 3;

    private int maxDelayTime = 7 * 24 * 3600;
}
