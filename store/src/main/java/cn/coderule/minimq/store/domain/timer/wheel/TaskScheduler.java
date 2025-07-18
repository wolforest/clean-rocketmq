package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;

public class TaskScheduler {
    private final StoreConfig storeConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;

    public TaskScheduler(StoreConfig storeConfig, TimerLog timerLog, TimerWheel timerWheel) {
        this.storeConfig = storeConfig;
        this.timerLog = timerLog;
        this.timerWheel = timerWheel;
    }

    public boolean addTimer(TimerEvent event) {
        return false;
    }
}
