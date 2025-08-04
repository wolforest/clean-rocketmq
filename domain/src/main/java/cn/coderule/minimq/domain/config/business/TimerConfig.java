package cn.coderule.minimq.domain.config.business;

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

    private int timerLogFileSize = 100 * 1024 * 1024;
    /**
     * copy from CommitLogConfig, this is ugly
     * and because module timer were partly moved to broker
     */
    private int commitLogFileSize = 100 * 1024 * 1024;

    private int totalSlots = 7 * 24 * 3600;
    private int wheelSlots = 2 * 24 * 3600;
    private int precision = 1_000;

    private int flushInterval = 1_000;
    private int consumerThreadNum = 3;
    private int producerThreadNum = 3;

    private int maxDelayTime = 7 * 24 * 3600;
}
