package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.domain.timer.service.CheckpointService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerRecover {
    private final TimerConfig timerConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;
    private final CheckpointService checkpointService;

    public TimerRecover(
        StoreConfig storeConfig,
        TimerLog timerLog,
        TimerWheel timerWheel,
        CheckpointService checkpointService
    ) {
        this.timerConfig = storeConfig.getTimerConfig();
        this.timerLog = timerLog;
        this.timerWheel = timerWheel;
        this.checkpointService = checkpointService;
    }

    public void recover() {
    }
}
