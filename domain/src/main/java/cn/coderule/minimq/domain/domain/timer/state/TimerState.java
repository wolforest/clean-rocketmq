package cn.coderule.minimq.domain.domain.timer.state;

import java.io.Serializable;
import lombok.Data;

@Data
public class TimerState implements Serializable {
    private volatile long timerQueueOffset;
    private volatile long committedQueueOffset;

    private volatile long lastScanTime;
    private volatile long lastTaskTime;

    private volatile long preloadTime;
    private volatile long commitTime;
    private volatile long lastCommitTime;
    private volatile long lastCommittedQueueOffset;

    private boolean dequeueFlag;
}
