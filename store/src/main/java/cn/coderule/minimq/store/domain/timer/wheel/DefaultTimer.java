package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.service.store.domain.timer.Timer;
import cn.coderule.minimq.store.domain.timer.service.CheckpointService;

public class DefaultTimer implements Timer {
    private final StoreConfig storeConfig;
    private final CheckpointService checkpointService;

    private final TaskScheduler taskScheduler;
    private final WheelScanner wheelScanner;


    public DefaultTimer(StoreConfig storeConfig, CheckpointService checkpointService) {
        this.storeConfig = storeConfig;
        this.checkpointService = checkpointService;

        this.wheelScanner = new WheelScanner(storeConfig);
        this.taskScheduler = new TaskScheduler(storeConfig);
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
