package com.wolfmq.store.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.store.api.TimerStoreImpl;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.timer.service.CheckpointService;
import com.wolfmq.store.domain.timer.service.TimerService;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;

public class TimerBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private TimerService timerService;

    public TimerBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        MessageService messageService = context.getBean(MessageService.class, ContextNamespace.APP);

        String checkpointPath = StorePath.getTimerCheckPath();
        CheckpointService checkpointService = new CheckpointService(checkpointPath);

        timerService = new TimerService(storeConfig, checkpointService, messageService);
        context.register(timerService, ContextNamespace.APP);

        TimerStore timerStore = new TimerStoreImpl(timerService);
        context.register(timerStore, TimerStore.class, ContextNamespace.API);
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
