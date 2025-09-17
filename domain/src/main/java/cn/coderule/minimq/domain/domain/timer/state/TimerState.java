package cn.coderule.minimq.domain.domain.timer.state;

import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import java.io.Serializable;
import lombok.Data;

/**
 * concepts of timer:
 *  - timer queue: system topic for timer task
 *  - timer message: message in timer queue
 *  - save: save timer task to persistent storage
 *  - scan: scan timer task from persistent storage
 *  - commit: saved or passed to checkpoint service
 */
@Data
public class TimerState implements Serializable {
    private volatile int state = TimerConstants.INITIAL;

    private final int precision;
    private final int totalSlots;
    private final int wheelSlots;

    /**
     * @rocketmq original name shouldRunningDequeue
     * True if current store is master
     *  or current brokerId is equal to the minimum brokerId
     *  of the replica group in slaveActingMaster mode.
     */
    private volatile boolean enableScan = true;
    /**
     * @rocketmq original name dequeueStatusChangeFlag
     * the dequeue is an asynchronous process,
     * use this flag to track if the status has changed
     */
    private volatile boolean hasDequeueException = false;

    /**
     * the latest time when pull message from message queue
     * @rocketmq original name lastEnqueueButExpiredTime
     */
    public long latestTimerMessageTime;
    /**
     * the time of the latest messageExt.storeTimeStamp
     * @rocketmq original name lastEnqueueButExpiredStoreTime
     */
    public long latestTimerMessageStoreTime;

    /**
     * last timer task scan time
     * @rocketmq original name currReadTimeMs
     */
    private volatile long lastScanTime;
    /**
     * last timer task save time
     * @rocketmq original name currWriteTimeMs
     */
    private volatile long lastSaveTime;

    /**
     * timer task preload time
     * @rocketmq original name preReadTimeMs
     */
    private volatile long preloadTime;


    /**
     * timer queue offset
     * @rocketmq original name currQueueOffset
     */
    private volatile long timerQueueOffset;

    /**
     * saved timer queue offset(not flushed)
     * @rocketmq original name commitQueueOffset
     */
    private volatile long savedQueueOffset;

    /**
     * timer task commit time, for checkpoint
     * equals currReadTimeMs in all code base,
     *      maybe useful in commercial version.
     * @rocketmq original name commitReadTime
     */
    private volatile long commitSaveTime;

    /**
     * last timer task committed save time, for checkpoint
     * @rocketmq original name lastCommitReadTimeMs
     */
    private volatile long lastCommitSaveTime;

    /**
     * last committed queue offset, for checkpoint
     * @rocketmq original name lastCommitQueueOffset
     */
    private volatile long lastCommitQueueOffset;


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

    public void start() {
        this.state = TimerConstants.RUNNING;
    }

    public boolean isRunning() {
        return TimerConstants.RUNNING == state;
    }

    public void moveScanTime() {
        this.lastScanTime = lastScanTime + precision;
        this.commitSaveTime = lastScanTime;
    }

    public void tryMoveSaveTime() {
        long now = formatTime(System.currentTimeMillis());
        if (lastSaveTime < now) {
            lastSaveTime = now;
        }
    }

    private long formatTime(long time) {
        return time / precision * precision;
    }
}
