package com.wolfmq.store.domain.timer.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import com.wolfmq.domain.config.server.StoreConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlushService extends ServiceThread {
    private final StoreConfig storeConfig;
    private final TimerService timerService;

    public FlushService(StoreConfig storeConfig, TimerService timerService) {
        this.storeConfig = storeConfig;
        this.timerService = timerService;
    }

    @Override
    public String getServiceName() {
        return FlushService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
