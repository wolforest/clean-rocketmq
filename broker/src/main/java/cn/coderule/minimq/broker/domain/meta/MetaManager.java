package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.core.exception.InvalidConfigException;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;

public class MetaManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        RouteService routeService = initRouteService();
        BrokerContext.register(routeService);

        TopicConfig topicConfig = BrokerContext.getBean(TopicConfig.class);
        RouteController routeController = new RouteController(topicConfig, routeService);
        BrokerContext.register(routeController);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    private RouteService initRouteService() {
        RouteLoader routeLoader = null;
        if (StringUtil.notBlank(brokerConfig.getRegistryAddress())) {
            routeLoader = BrokerContext.getBean(RouteLoader.class);
        }

        RouteMocker routeMocker = null;
        if (brokerConfig.isEnableEmbedStore()) {
            EmbedTopicStore embedTopicStore = BrokerContext.getBean(EmbedTopicStore.class);
            TopicConfig topicConfig = BrokerContext.getBean(TopicConfig.class);
            routeMocker = new RouteMocker(brokerConfig, topicConfig, embedTopicStore);
        }

        if (null == routeLoader && null == routeMocker) {
            throw new InvalidConfigException("invalid config: registryAddress and enableEmbedStore");
        }

        return new RouteService(brokerConfig, routeLoader, routeMocker);
    }

}
