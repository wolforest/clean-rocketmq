package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class ReviveManager implements Lifecycle {
    @Override
    public void initialize() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());


    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }


}
