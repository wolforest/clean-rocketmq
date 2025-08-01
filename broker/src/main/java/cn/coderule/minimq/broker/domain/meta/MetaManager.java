package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.core.exception.InvalidConfigException;
import cn.coderule.minimq.domain.domain.meta.topic.TopicMap;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;

public class MetaManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        RouteService routeService = initRouteService();
        TopicService topicService = initTopicService();
        SubscriptionService subscriptionService = initSubscriptionService();

        RouteController routeController = new RouteController(routeService, topicService, subscriptionService);
        BrokerContext.register(routeController);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    private TopicService initTopicService() {
        return null;
    }

    private SubscriptionService initSubscriptionService() {
        return null;
    }

    private RouteService initRouteService() {
        RouteLoader routeLoader = null;
        if (StringUtil.notBlank(brokerConfig.getRegistryAddress())) {
            routeLoader = BrokerContext.getBean(RouteLoader.class);
        }

        RouteMocker routeMocker = null;
        if (brokerConfig.isEnableEmbedStore()) {
            EmbedTopicStore embedTopicStore = BrokerContext.getBean(EmbedTopicStore.class);
            TopicConfig topicConfig = brokerConfig.getTopicConfig();
            routeMocker = new RouteMocker(brokerConfig, topicConfig, embedTopicStore);
        }

        if (null == routeLoader && null == routeMocker) {
            throw new InvalidConfigException("invalid config: registryAddress and enableEmbedStore");
        }

        RouteService routeService = new RouteService(brokerConfig, routeLoader, routeMocker);
        BrokerContext.register(routeService);
        return routeService;
    }

}
