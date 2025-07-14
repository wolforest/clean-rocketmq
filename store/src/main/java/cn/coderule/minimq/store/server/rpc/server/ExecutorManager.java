package cn.coderule.minimq.store.server.rpc.server;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class ExecutorManager implements Lifecycle {
    private final StoreConfig storeConfig;

    @Getter
    private ExecutorService enqueueExecutor;
    @Getter
    private ExecutorService pullExecutor;
    @Getter
    private ExecutorService adminExecutor;

    public ExecutorManager(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public void initialize() throws Exception {
        initPullExecutor();
        initAdminExecutor();
        initEnqueueExecutor();
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception {
        if (null != adminExecutor) adminExecutor.shutdown();
        if (null != enqueueExecutor) enqueueExecutor.shutdown();
        if (null != pullExecutor) pullExecutor.shutdown();
    }

    private void initAdminExecutor() {
        BlockingQueue<Runnable> adminQueue = new LinkedBlockingQueue<>(storeConfig.getAdminQueueCapacity());
        adminExecutor = ThreadUtil.newThreadPoolExecutor(
            storeConfig.getAdminThreadNum(),
            storeConfig.getAdminThreadNum(),
            60 * 1000,
            TimeUnit.MILLISECONDS,
            adminQueue,
            new DefaultThreadFactory("StoreAdminThread_")
        );
    }

    private void initPullExecutor() {
        BlockingQueue<Runnable> pullQueue = new LinkedBlockingQueue<>(storeConfig.getPullQueueCapacity());
        pullExecutor = ThreadUtil.newThreadPoolExecutor(
            storeConfig.getPullThreadNum(),
            storeConfig.getPullThreadNum(),
            60 * 1000,
            TimeUnit.MILLISECONDS,
            pullQueue,
            new DefaultThreadFactory("StorePullThread_")
        );
    }

    private void initEnqueueExecutor() {
        BlockingQueue<Runnable> enqueueQueue = new LinkedBlockingQueue<>(storeConfig.getEnqueueQueueCapacity());
        enqueueExecutor = ThreadUtil.newThreadPoolExecutor(
            storeConfig.getEnqueueThreadNum(),
            storeConfig.getEnqueueThreadNum(),
            60 * 1000,
            TimeUnit.MILLISECONDS,
            enqueueQueue,
            new DefaultThreadFactory("StoreEnqueueThread_")
        );
    }

}
