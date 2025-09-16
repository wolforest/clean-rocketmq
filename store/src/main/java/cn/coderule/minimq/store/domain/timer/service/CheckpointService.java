package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;

public class CheckpointService {
    private final StoreConfig storeConfig;

    public CheckpointService(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    public void store(TimerCheckpoint checkpoint) {

    }

    public TimerCheckpoint load() {
        return null;
    }
}
