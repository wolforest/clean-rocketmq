package cn.coderule.minimq.domain.domain.timer.state;

import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import java.io.Serializable;
import lombok.Data;

@Data
public class TimerState implements Serializable {
    private volatile int state = TimerConstants.INITIAL;

    private final int precision;
    private final int totalSlots;
    private final int wheelSlots;

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

    public TimerState(TimerConfig timerConfig) {
        this.precision = timerConfig.getPrecision();
        this.totalSlots = timerConfig.getTotalSlots();

        int wheelSlots = timerConfig.getWheelSlots();
        if (wheelSlots > totalSlots - TimerConstants.TIMER_BLANK_SLOTS
            || wheelSlots < 2) {
            this.wheelSlots = totalSlots - TimerConstants.TIMER_BLANK_SLOTS;
        } else {
            this.wheelSlots = wheelSlots;
        }
    }

    public boolean isRunning() {
        return TimerConstants.RUNNING == state;
    }

    private long formatTime(long time) {
        return time / precision * precision;
    }
}
