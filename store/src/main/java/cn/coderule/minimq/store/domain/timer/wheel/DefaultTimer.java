package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.service.store.domain.timer.Timer;
import cn.coderule.minimq.store.domain.timer.service.CheckpointService;
import cn.coderule.minimq.store.server.bootstrap.StorePath;
import java.io.IOException;

public class DefaultTimer implements Timer {
    private final CheckpointService checkpointService;

    private final TaskScheduler taskScheduler;
    private final WheelScanner wheelScanner;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;


    public DefaultTimer(StoreConfig storeConfig, CheckpointService checkpointService) throws IOException {
        TimerConfig timerConfig = storeConfig.getTimerConfig();
        this.checkpointService = checkpointService;

        this.timerLog = new TimerLog(
            StorePath.getTimerLogPath(),
            timerConfig.getTimerLogFileSize()
        );

        this.timerWheel = new TimerWheel(
            StorePath.getTimerWheelPath(),
            timerConfig.getTotalSlots(),
            timerConfig.getPrecision()
        );

        this.wheelScanner = new WheelScanner(storeConfig, timerLog, timerWheel);
        this.taskScheduler = new TaskScheduler(storeConfig, timerLog, timerWheel);
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
    public void storeCheckpoint(TimerCheckpoint checkpoint) {
        checkpointService.store(checkpoint);
    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return checkpointService.load();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return taskScheduler.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return wheelScanner.scan(delayTime);
    }


}
