package cn.coderule.minimq.store.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.rpc.server.ConnectionManager;
import cn.coderule.minimq.store.server.rpc.server.ExecutorManager;
import cn.coderule.minimq.store.server.rpc.server.StoreServer;

public class RpcManager implements Lifecycle {
    private StoreConfig storeConfig;
    private ExecutorManager executorManager;
    private StoreServer storeServer;

    @Override
    public void initialize() {
        storeConfig = StoreContext.getBean(StoreConfig.class);

        executorManager = new ExecutorManager(storeConfig);
        executorManager.initialize();

        ConnectionManager connectionManager = new ConnectionManager();

    }

    @Override
    public void start() {
        if (null != executorManager) executorManager.start();
        if (null != storeServer) storeServer.start();
    }

    @Override
    public void shutdown() {
        if (null != executorManager) executorManager.shutdown();
        if (null != storeServer) storeServer.shutdown();
    }


}
