package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.wheel.Slot;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        ScanResult result = new ScanResult();
        Slot slot = timerWheel.getSlot(delayTime);
        if (-1 == slot.getTimeMs()) {
            return result;
        }

        result.setCode(1);

        try {
            scanBySlot(result, slot);
        } catch (Throwable e) {
            log.error("scan timer log error", e);
        }

        return result;
    }

    private void scanBySlot(ScanResult result, Slot slot) {

    }
}
