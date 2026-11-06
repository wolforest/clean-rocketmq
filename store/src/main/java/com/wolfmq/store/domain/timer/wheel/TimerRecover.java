package com.wolfmq.store.domain.timer.wheel;

import com.wolfmq.common.config.business.TimerConfig;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.store.domain.messagequeue.queue.MessageService;
import com.wolfmq.store.domain.timer.service.CheckpointService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerRecover {
    private final TimerConfig timerConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;
    private final CheckpointService checkpointService;
    private final MessageService messageService;

    public TimerRecover(
        StoreConfig storeConfig,
        TimerLog timerLog,
        TimerWheel timerWheel,
        CheckpointService checkpointService,
        MessageService messageService
    ) {
        this.timerConfig = storeConfig.getTimer();

        this.timerLog = timerLog;
        this.timerWheel = timerWheel;

        this.messageService = messageService;
        this.checkpointService = checkpointService;
    }

    public void recover() {
    }
}
