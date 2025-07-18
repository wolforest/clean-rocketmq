package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.wheel.Slot;
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
        if (needRoll(event)) {
            magic = magic | TimerConstants.MAGIC_ROLL;
        }

        magic = addDeleteFlag(event.getMessageBO(), magic);
        Slot slot = timerWheel.getSlot(event.getDelayTime());

        return false;
    }

    private boolean needRoll(TimerEvent event) {
        return event.getDelayTime() - event.getBatchTime()
            > (long) timerConfig.getWheelSlots() * timerConfig.getPrecision();
    }

    private int addDeleteFlag(MessageBO messageBO, int magic) {
        String key = messageBO.getProperty(TimerConstants.TIMER_DELETE_UNIQUE_KEY);
        if (key == null) {
            return magic;
        }

        return magic | TimerConstants.MAGIC_DELETE;
    }

}
