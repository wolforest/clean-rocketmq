package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class ExecutorFactory implements Lifecycle {
    private final RegistryConfig config;

    @Getter
    private ExecutorService defaultExecutor;
    @Getter
    private ExecutorService routeExecutor;

    public ExecutorFactory(RegistryConfig config) {
        this.config = config;
    }

    @Override
    public void initialize() {
        initDefaultExecutor();
        initRouteExecutor();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        defaultExecutor.shutdown();
        routeExecutor.shutdown();
    }

    private void initDefaultExecutor() {
        BlockingQueue<Runnable> defaultQueue = new LinkedBlockingQueue<>(config.getProcessorQueueCapacity());
        defaultExecutor = ThreadUtil.newThreadPoolExecutor(
            config.getProcessThreadNum(),
            config.getProcessThreadNum(),
            60,
            TimeUnit.SECONDS,
            defaultQueue,
            new DefaultThreadFactory("RegistryProcessorThread_")
        );
    }

    private void initRouteExecutor() {
        BlockingQueue<Runnable> routeQueue = new LinkedBlockingQueue<>(config.getRouteQueueCapacity());
        routeExecutor = ThreadUtil.newThreadPoolExecutor(
            config.getRouteThreadNum(),
            config.getRouteThreadNum(),
            60,
            TimeUnit.SECONDS,
            routeQueue,
            new DefaultThreadFactory("RegistryRouteThread_")
        );
    }
}
