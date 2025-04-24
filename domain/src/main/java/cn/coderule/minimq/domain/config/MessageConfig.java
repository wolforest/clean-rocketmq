package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import java.time.Duration;
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

}
