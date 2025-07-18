package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;

public class TaskScheduler {
    private final StoreConfig storeConfig;

    public TaskScheduler(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    public boolean addTimer(TimerEvent event) {
        return false;
    }
}
