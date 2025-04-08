package cn.coderule.minimq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreScheduler implements Lifecycle {
    private final StoreConfig storeConfig;
    @Getter
    private final ScheduledExecutorService scheduler;
    private final TimeUnit defaultUnit = TimeUnit.SECONDS;

    public StoreScheduler(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;

        int poolSize = storeConfig.getSchedulerPoolSize();
        this.scheduler = new ScheduledThreadPoolExecutor(poolSize);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        scheduler.shutdown();

        try {
            int shutdownTimeout = storeConfig.getSchedulerShutdownTimeout();
            boolean status = scheduler.awaitTermination(shutdownTimeout, defaultUnit);

            if (!status) {
                log.error("shutdown store scheduler failed");
            }

            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            log.error("shutdown store scheduler error", e);
        }
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay) {
        return schedule(runnable, delay, defaultUnit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return scheduler.schedule(runnable, delay, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period) {
        return scheduleAtFixedRate(runnable, initialDelay, period, defaultUnit);
    }
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        return scheduler.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay) {
        return scheduleWithFixedDelay(runnable, initialDelay, delay, defaultUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
        return scheduler.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }


}
