package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.model.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicService {
    private final RegistryConfig config;
    private final Route route;

    public TopicService(RegistryConfig config, Route route) {
        this.route = route;
        this.config = config;

    }

}
