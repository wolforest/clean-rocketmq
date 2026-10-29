package com.wolfmq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.store.server.bootstrap.StoreRegister;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
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
        this.storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        StoreRegister storeRegister = context.getBean(StoreRegister.class, ContextNamespace.APP);

        CommitLogSynchronizer commitLogSynchronizer = new CommitLogSynchronizer(storeConfig);
        context.register(commitLogSynchronizer, ContextNamespace.APP);


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
