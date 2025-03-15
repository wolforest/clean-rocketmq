package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicManager {
    private final RegistryConfig config;
    private final Route route;

    public TopicManager(RegistryConfig config, Route route) {
        this.route = route;
        this.config = config;

    }

}
