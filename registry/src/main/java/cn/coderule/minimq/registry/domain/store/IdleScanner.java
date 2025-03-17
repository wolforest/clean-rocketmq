package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IdleScanner {
    private final RegistryConfig config;
    private final StoreRegistry registry;
    private final ScheduledExecutorService scheduler;

    public IdleScanner(RegistryConfig config, StoreRegistry registry) {
        this.config = config;
        this.registry = registry;
        this.scheduler = initScheduler();
    }

    public void start() {
        this.scheduler.scheduleWithFixedDelay(
            IdleScanner.this::scan,
            5,
            config.getIdleScanInterval(),
            TimeUnit.MICROSECONDS
        );
    }

    public void shutdown() {
        this.scheduler.shutdown();
    }

    public void scan() {

    }

    private ScheduledExecutorService initScheduler() {
        return ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("IdleScanner")
        );
    }
}
