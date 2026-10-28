package com.wolfmq.store.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.store.api.TimerStoreImpl;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.timer.service.CheckpointService;
import com.wolfmq.store.domain.timer.service.TimerService;
import com.wolfmq.store.server.bootstrap.StoreContext;

public class TimerBootstrap implements Lifecycle {
    private TimerService timerService;
    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        MessageService messageService = StoreContext.getBean(MessageService.class);

        String checkpointPath = StorePath.getTimerCheckPath();
        CheckpointService checkpointService = new CheckpointService(checkpointPath);

        timerService = new TimerService(storeConfig, checkpointService, messageService);
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
