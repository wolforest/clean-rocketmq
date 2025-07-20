package cn.coderule.minimq.domain.domain.timer.state;

import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import java.io.Serializable;
import lombok.Data;

@Data
public class TimerState implements Serializable {
    private volatile int state = TimerConstants.INITIAL;

    /**
     * True if current store is master
     *  or current brokerId is equal to the minimum brokerId
     *  of the replica group in slaveActingMaster mode.
     */
    private volatile boolean shouldDequeue;
    /**
     * the dequeue is an asynchronous process,
     * use this flag to track if the status has changed
     */
    private volatile boolean dequeueExceptionFlag = false;

    private volatile long timerQueueOffset;
    private volatile long committedQueueOffset;

    private volatile long lastScanTime;
    private volatile long lastTaskTime;

    private volatile long preloadTime;
    private volatile long commitTime;
    private volatile long lastCommitTime;
    private volatile long lastCommittedQueueOffset;

    /**
     * the latest time when pull message from message queue
     */
    public long latestTimerMessageTime;
    /**
     * the time of the latest messageExt.storeTimeStamp
     */
    public long latestTimerMessageStoreTime;

    private boolean dequeueFlag;

    public boolean isRunning() {
        return TimerConstants.RUNNING == state;
    }
}
