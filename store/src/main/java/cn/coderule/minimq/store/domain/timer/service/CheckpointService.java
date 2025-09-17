package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.common.convention.ability.Flushable;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckpointService implements Flushable {
    private final StoreConfig storeConfig;
    private final String path;

    public CheckpointService(StoreConfig storeConfig, String path) {
        this.storeConfig = storeConfig;
        this.path = path;
    }

    public void store(TimerCheckpoint checkpoint) {

    }

    public TimerCheckpoint load() {
        return null;
    }

    public void flush() {

    }
}
