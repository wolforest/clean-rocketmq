package com.wolfmq.store.domain.timer.wheel;

import cn.coderule.common.convention.ability.Flushable;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.Timer;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.timer.service.CheckpointService;
import java.io.IOException;

public class DefaultTimer implements Timer, Flushable {
    private final StoreConfig storeConfig;

    private final TimerLog timerLog;
    private final TimerWheel timerWheel;
    private final CheckpointService checkpointService;
    private final MessageService messageService;

    private final TaskAdder taskAdder;
    private final TaskScanner taskScanner;
    private final TimerRecover recover;

    public DefaultTimer(
        StoreConfig storeConfig,
        CheckpointService checkpointService,
        MessageService messageService
    ) throws IOException {
        this.storeConfig = storeConfig;

        this.checkpointService = checkpointService;
        this.messageService = messageService;

        this.timerLog = initTimerLog();
        this.timerWheel = initTimerWheel();
        this.recover = initRecover();

        this.taskScanner = new TaskScanner(storeConfig, timerLog, timerWheel);
        this.taskAdder = new TaskAdder(storeConfig, timerLog, timerWheel);
    }

    @Override
    public void initialize() throws Exception {
        timerLog.load();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        timerWheel.shutdown();
        timerLog.shutdown();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return taskAdder.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return taskScanner.scan(delayTime);
    }

    @Override
    public void recover() {
        recover.recover();
    }

    @Override
    public void flush() throws Exception {
        updateCheckpoint();

        timerLog.flush();
        timerWheel.flush();
    }

    private void updateCheckpoint() {
        long logPos = timerLog.getFlushPosition();

        TimerCheckpoint checkpoint = new TimerCheckpoint();
        checkpoint.setLastTimerLogFlushPos(logPos);

        checkpointService.update(checkpoint);
    }

    private TimerLog initTimerLog() {
        TimerConfig timerConfig = storeConfig.getTimer();

        return new TimerLog(
            StorePath.getTimerLogPath(),
            timerConfig.getTimerLogFileSize()
        );
    }

    private TimerWheel initTimerWheel() throws IOException {
        TimerConfig timerConfig = storeConfig.getTimer();

        return new TimerWheel(
            StorePath.getTimerWheelPath(),
            timerConfig.getTotalSlots(),
            timerConfig.getPrecision()
        );
    }

    private TimerRecover initRecover() {
        return new TimerRecover(
            storeConfig,
            timerLog,
            timerWheel,
            checkpointService,
            messageService
        );
    }
}
