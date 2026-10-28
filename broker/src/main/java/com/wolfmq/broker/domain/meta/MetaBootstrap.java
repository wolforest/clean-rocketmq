package com.wolfmq.broker.domain.meta;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.broker.api.RouteController;
import com.wolfmq.broker.infra.embed.EmbedTopicStore;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.broker.server.bootstrap.BrokerContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.core.exception.InvalidConfigException;
import com.wolfmq.rpc.registry.route.RouteLoader;

/**
 * dependency management for meta
 *  - initialize meta servers
 *  - nothing to start/shutdown
 */
public class MetaBootstrap implements Lifecycle {
    private BrokerConfig brokerConfig;
    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        RouteService routeService = initRouteService();
        BrokerTopicService topicService = initTopicService();
        SubscriptionService subscriptionService = initSubscriptionService();

        RouteController routeController = new RouteController(routeService, topicService, subscriptionService);
        BrokerContext.registerAPI(routeController);
    }

    @Override
    public void start() throws Exception {
        // nothing to do
    }

    @Override
    public void shutdown() throws Exception {
        // nothing to do
    }

    private BrokerTopicService initTopicService() {
        TopicStore topicStore = BrokerContext.getBean(TopicStore.class);
        return new BrokerTopicService(topicStore);
    }

    private SubscriptionService initSubscriptionService() {
        SubscriptionStore subscriptionStore = BrokerContext.getBean(SubscriptionStore.class);
        return new SubscriptionService(subscriptionStore);
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
