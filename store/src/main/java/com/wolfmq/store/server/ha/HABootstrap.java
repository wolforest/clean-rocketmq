package com.wolfmq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.store.server.bootstrap.StoreRegister;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.store.server.ha.core.ClusterService;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;

public class HABootstrap implements Lifecycle {
    private final ApplicationContext context;

    private StoreConfig storeConfig;
    private ClusterService clusterService;

    public HABootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        this.storeConfig = context.getBean(StoreConfig.class);
        StoreRegister storeRegister = context.getBean(StoreRegister.class);

        CommitLogSynchronizer commitLogSynchronizer = new CommitLogSynchronizer(storeConfig);
        context.register(commitLogSynchronizer);


        clusterService = new ClusterService(storeConfig, storeRegister);
    }

    @Override
    public void start() throws Exception {
        clusterService.start();
    }

    @Override
    public void shutdown() throws Exception {
        clusterService.shutdown();
    }

}
