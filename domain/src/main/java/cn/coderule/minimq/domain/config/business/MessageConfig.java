package cn.coderule.minimq.domain.config.business;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Data;

@Data
public class MessageConfig implements Serializable {
    /**
     * max request size:
     * 130M = 4M * 32 messages + 2M property
     */
    private int maxRequestSize = 130 * 1024 * 1024;
    private int maxBodySize = 4 * 1024 * 1024;
    private int maxPropertySize = 16 * 1024;
    private int maxPropertyCount = 128;

    /**
     * max message group size, 0 or negative number means no limit for proxy
     */
    private int maxMessageGroupSize = 64;

    private long defaultInvisibleTime = Duration.ofSeconds(60).toMillis();
    private long minInvisibleTime = Duration.ofSeconds(10).toMillis();
    private long maxInvisibleTime = Duration.ofHours(12).toMillis();
    // has moved to TimerConfig
    // private long maxDelayTimeMills = Duration.ofDays(1).toMillis();
    private long maxTransactionRecoverySecond = Duration.ofHours(1).getSeconds();

    private boolean enableMessageTypeCheck = true;
    private int maxGetSize;


    private long longPollingReserveTimeInMillis = 100;

    private long invisibleTimeMillisWhenClear = 1000L;
    /**
     * message invisibleTime related config
     */
    private boolean enableAutoRenew = true;
    private int maxRenewRetryTime = 3;
    private int minRenewThreadNum = 2;
    private int maxRenewThreadNum = 4;
    private int renewQueueCapacity = 300;
    private long renewLockTimeout = TimeUnit.SECONDS.toMillis(3);
    private long renewAheadTime = TimeUnit.SECONDS.toMillis(10);
    private long maxRenewTime = TimeUnit.HOURS.toMillis(3);
    private long renewInterval = TimeUnit.SECONDS.toMillis(5);
    private long invisibleTimeOfClear = 1_000L;

    private long reviveInterval = 1000;
    private long reviveMaxSlow = 3;
    private long reviveScanTime = 10000;
    private boolean enableSkipLongAwaitingAck = false;
    private long reviveAckWaitMs = TimeUnit.MINUTES.toMillis(3);


    private int maxPopSize = 32;
    private boolean enablePopLog = false;
    private int popRetryProbability = 20;

    private boolean enablePopThreshold = false;
    private long popInflightThreshold = 10_000;

    private int popPollingSize = 1024;
    private int popPollingMapSize = 100000;
    // 20w cost 200M heap memory.
    private long maxPopPollingSize = 100000;

    private boolean enablePopBufferMerge = false;
    private int popCkStayBufferTime = 10 * 1000;
    private int popCkStayBufferTimeOut = 3 * 1000;
    /**
     * max check point number store in memory
     */
    private int popCkMaxBufferSize = 200000;
    private int popCkOffsetMaxQueueSize = 20000;
    private boolean enablePopBatchAck = false;
    private boolean enableNotifyAfterPopOrderLockRelease = true;
    private boolean initPopOffsetByCheckMsgInMem = true;

}
