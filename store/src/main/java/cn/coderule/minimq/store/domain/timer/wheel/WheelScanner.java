package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;

public class WheelScanner {
    private final StoreConfig storeConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;

    public WheelScanner(StoreConfig storeConfig, TimerLog timerLog, TimerWheel timerWheel) {
        this.storeConfig = storeConfig;
        this.timerLog = timerLog;
        this.timerWheel = timerWheel;
    }

    public ScanResult scan(long delayTime) {
        return null;
    }
}
