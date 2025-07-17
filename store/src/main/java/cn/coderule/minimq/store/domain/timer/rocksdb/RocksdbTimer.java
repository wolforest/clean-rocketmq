package cn.coderule.minimq.store.domain.timer.rocksdb;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.service.store.domain.timer.Timer;
import cn.coderule.minimq.store.domain.timer.CheckpointService;

public class RocksdbTimer implements Timer {
    private final StoreConfig storeConfig;
    private final CheckpointService checkpointService;

    public RocksdbTimer(StoreConfig storeConfig, CheckpointService checkpointService) {
        this.storeConfig = storeConfig;
        this.checkpointService = checkpointService;
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
        return false;
    }

    @Override
    public ScanResult scan() {
        return null;
    }
}
