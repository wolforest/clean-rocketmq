package cn.coderule.minimq.domain.config.message;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Data;

@Data
public class MessageConfig implements Serializable {
    private int maxSize;
    private int maxBodySize;
    private int maxPropertySize = 16 * 1024;
    private int maxPropertyCount = 128;

    /**
     * max message group size, 0 or negative number means no limit for proxy
     */
    private int maxMessageGroupSize = 64;

    private long defaultInvisibleTimeMills = Duration.ofSeconds(60).toMillis();
    private long minInvisibleTimeMillsForRecv = Duration.ofSeconds(10).toMillis();
    private long maxInvisibleTimeMills = Duration.ofHours(12).toMillis();
    private long maxDelayTimeMills = Duration.ofDays(1).toMillis();
    private long maxTransactionRecoverySecond = Duration.ofHours(1).getSeconds();

    private boolean enableMessageTypeCheck = true;
    private int maxGetSize;

    private long longPollingReserveTimeInMillis = 100;

    private long invisibleTimeMillisWhenClear = 1000L;
    /**
     * message invisibleTime related config
     */
    private boolean enableProxyAutoRenew = true;
    private int maxRenewRetryTimes = 3;
    private int renewThreadPoolNums = 2;
    private int renewMaxThreadPoolNums = 4;
    private int renewThreadPoolQueueCapacity = 300;
    private long lockTimeoutMsInHandleGroup = TimeUnit.SECONDS.toMillis(3);
    private long renewAheadTimeMillis = TimeUnit.SECONDS.toMillis(10);
    private long renewMaxTimeMillis = TimeUnit.HOURS.toMillis(3);
    private long renewSchedulePeriodMillis = TimeUnit.SECONDS.toMillis(5);

    private int reviveThreadNum = 8;
    private int popPollingSize = 1024;
    private int popPollingMapSize = 100000;
    // 20w cost 200M heap memory.
    private long maxPopPollingSize = 100000;
    private int reviveQueueNum = 8;
    private long reviveInterval = 1000;
    private long reviveMaxSlow = 3;
    private long reviveScanTime = 10000;
    private boolean enableSkipLongAwaitingAck = false;
    private long reviveAckWaitMs = TimeUnit.MINUTES.toMillis(3);
    private boolean enablePopLog = false;

    private int timerQueueNum = 1;
    private int transactionQueueNum = 1;

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

    // read message from pop retry topic v1, for the compatibility, will be removed in the future version
    private boolean retrieveMessageFromPopRetryTopicV1 = true;
    private boolean enableRetryTopicV2 = false;
}
