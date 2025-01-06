package com.wolf.minimq.store.server;

import com.wolf.minimq.domain.config.StoreConfig;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreScheduler {
    private final StoreConfig storeConfig;
    private final ScheduledExecutorService service;
    private final TimeUnit defaultUnit = TimeUnit.SECONDS;

    public StoreScheduler(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;

        int poolSize = storeConfig.getSchedulerPoolSize();
        this.service = new ScheduledThreadPoolExecutor(poolSize);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay) {
        return schedule(runnable, delay, defaultUnit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return service.schedule(runnable, delay, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period) {
        return scheduleAtFixedRate(runnable, initialDelay, period, defaultUnit);
    }
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        return service.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay) {
        return scheduleWithFixedDelay(runnable, initialDelay, delay, defaultUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
        return service.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }

    public void shutdown() {
        service.shutdown();

        try {
            int shutdownTimeout = storeConfig.getSchedulerShutdownTimeout();
            boolean status = service.awaitTermination(shutdownTimeout, defaultUnit);

            if (!status) {
                log.error("shutdown store scheduler failed");
            }

            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            log.error("shutdown store scheduler error", e);
        }
    }

}
