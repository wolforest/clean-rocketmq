package cn.coderule.wolfmq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.store.server.bootstrap.StoreRegister;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import cn.coderule.wolfmq.store.server.ha.core.ClusterService;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;

public class HABootstrap implements Lifecycle {
    private StoreConfig storeConfig;
    private ClusterService clusterService;

    @Override
    public void initialize() throws Exception {
        this.storeConfig = StoreContext.getBean(StoreConfig.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);

        CommitLogSynchronizer commitLogSynchronizer = new CommitLogSynchronizer(storeConfig);
        StoreContext.register(commitLogSynchronizer);


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
