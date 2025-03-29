package cn.coderule.minimq.store.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.store.server.StoreContext;

public class RpcManager implements Lifecycle {
    @Override
    public void initialize() {
        ConnectionManager connectionManager = new ConnectionManager();
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }


}
