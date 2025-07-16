package cn.coderule.minimq.domain.domain.timer;

import cn.coderule.minimq.domain.domain.meta.DataVersion;
import java.io.Serializable;
import lombok.Data;

@Data
public class TimerCheckpoint implements Serializable {
    private final DataVersion version;

    private volatile long lastReadTime = 0L;
    private volatile long lastWritePosition = 0L;
    private volatile long lastTimerQueueOffset = 0L;
    private volatile long masterTimerQueueOffset = 0L;

    public TimerCheckpoint() {
        this.version = new DataVersion();
    }
}
