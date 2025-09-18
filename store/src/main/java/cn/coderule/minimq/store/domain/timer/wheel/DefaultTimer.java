package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.common.convention.ability.Flushable;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.store.domain.timer.Timer;
import cn.coderule.minimq.domain.config.store.StorePath;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.store.domain.mq.queue.MessageService;
import cn.coderule.minimq.store.domain.timer.service.CheckpointService;
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
        TimerConfig timerConfig = storeConfig.getTimerConfig();

        return new TimerLog(
            StorePath.getTimerLogPath(),
            timerConfig.getTimerLogFileSize()
        );
    }

    private TimerWheel initTimerWheel() throws IOException {
        TimerConfig timerConfig = storeConfig.getTimerConfig();

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
