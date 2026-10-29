package com.wolfmq.broker.domain.meta;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.broker.api.RouteController;
import com.wolfmq.broker.infra.embed.EmbedTopicStore;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.broker.infra.store.TopicStore;
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
    private final ApplicationContext context;

    public MetaBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    @Override
    public void initialize() throws Exception {
        brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);

        RouteService routeService = initRouteService();
        BrokerTopicService topicService = initTopicService();
        SubscriptionService subscriptionService = initSubscriptionService();

        RouteController routeController = new RouteController(routeService, topicService, subscriptionService);
        context.register(routeController, ContextNamespace.API);
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
        TopicStore topicStore = context.getBean(TopicStore.class, ContextNamespace.APP);
        return new BrokerTopicService(topicStore);
    }

    private SubscriptionService initSubscriptionService() {
        SubscriptionStore subscriptionStore = context.getBean(SubscriptionStore.class, ContextNamespace.APP);
        return new SubscriptionService(subscriptionStore);
    }

    private RouteService initRouteService() {
        RouteLoader routeLoader = null;
        if (StringUtil.notBlank(brokerConfig.getRegistryAddress())) {
            routeLoader = context.getBean(RouteLoader.class, ContextNamespace.APP);
        }

        RouteMocker routeMocker = null;
        if (brokerConfig.isEnableEmbedStore()) {
            EmbedTopicStore embedTopicStore = context.getBean(EmbedTopicStore.class, ContextNamespace.APP);
            TopicConfig topicConfig = brokerConfig.getTopicConfig();
            routeMocker = new RouteMocker(brokerConfig, topicConfig, embedTopicStore);
        }

        if (null == routeLoader && null == routeMocker) {
            throw new InvalidConfigException("invalid config: registryAddress and enableEmbedStore");
        }

        RouteService routeService = new RouteService(brokerConfig, routeLoader, routeMocker);
        context.register(routeService, ContextNamespace.APP);
        return routeService;
    }

}
