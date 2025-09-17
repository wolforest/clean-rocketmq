package cn.coderule.minimq.store.domain.timer;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.StorePath;
import cn.coderule.minimq.domain.domain.store.api.TimerStore;
import cn.coderule.minimq.domain.domain.store.domain.timer.TimerManager;
import cn.coderule.minimq.store.api.TimerStoreImpl;
import cn.coderule.minimq.store.domain.timer.service.CheckpointService;
import cn.coderule.minimq.store.domain.timer.service.TimerService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class DefaultTimerManager implements TimerManager {
    private TimerService timerService;
    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        String checkpointPath = StorePath.getTimerCheckPath();
        CheckpointService checkpointService = new CheckpointService(storeConfig, checkpointPath);

        timerService = new TimerService(storeConfig, checkpointService);
        StoreContext.register(timerService);

        TimerStore timerStore = new TimerStoreImpl(timerService);
        StoreContext.registerAPI(timerStore, TimerStore.class);
    }

    @Override
    public void start() throws Exception {
        timerService.start();
    }

    @Override
    public void shutdown() throws Exception {
        timerService.shutdown();
    }



}
