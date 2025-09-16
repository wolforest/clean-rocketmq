package cn.coderule.minimq.store.domain.timer;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.domain.timer.TimerManager;
import cn.coderule.minimq.store.domain.timer.service.CheckpointService;
import cn.coderule.minimq.store.domain.timer.service.TimerService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class DefaultTimerManager implements TimerManager {
    private TimerService timerService;
    @Override
    public void initialize() throws Exception {
        // init Timer
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);

        CheckpointService checkpointService = new CheckpointService(storeConfig);

        timerService = new TimerService(storeConfig, checkpointService);
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
