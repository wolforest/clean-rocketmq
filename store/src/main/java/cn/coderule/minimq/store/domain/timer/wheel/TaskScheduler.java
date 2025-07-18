package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskScheduler {
    private final StoreConfig storeConfig;
    private final TimerConfig timerConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;

    public TaskScheduler(StoreConfig storeConfig, TimerLog timerLog, TimerWheel timerWheel) {
        this.storeConfig = storeConfig;
        this.timerConfig = storeConfig.getTimerConfig();
        this.timerLog = timerLog;
        this.timerWheel = timerWheel;
    }

    public boolean addTimer(TimerEvent event) {
        log.debug("add timer event: delayTime={}, message={}",
            DateUtil.asLocalDateTime(event.getDelayTime()),
            event.getMessageBO()
        );

        int magic = TimerConstants.MAGIC_DEFAULT;
        long now = System.currentTimeMillis();
        if (needRoll(event, now)) {
            magic = magic | TimerConstants.MAGIC_ROLL;
        }

        return false;
    }

    private boolean needRoll(TimerEvent event, long now) {
        return event.getDelayTime() - now
            > (long) timerConfig.getWheelSlots() * timerConfig.getPrecision();
    }

}
