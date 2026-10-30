package com.wolfmq.registry.domain.store;


import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.domain.kv.KVService;
import com.wolfmq.registry.domain.store.model.Route;
import com.wolfmq.registry.domain.store.service.ChannelCloser;
import com.wolfmq.registry.domain.store.service.ClusterService;
import com.wolfmq.registry.domain.store.service.IdleScanner;
import com.wolfmq.registry.domain.store.service.RegistryTopicService;
import com.wolfmq.registry.processor.ClusterProcessor;
import com.wolfmq.registry.processor.RegistryProcessor;
import com.wolfmq.registry.processor.RouteProcessor;
import com.wolfmq.registry.processor.TopicProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.registry.server.rpc.HaClient;

public class StoreBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public StoreBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private RegistryConfig registryConfig;
    private Route route ;
    private ExecutorFactory factory;

    private StoreRegistry storeRegistry;
    private IdleScanner idleScanner;
    private ClusterService clusterService;
    private RegistryTopicService topicService;

    @Override
    public void initialize() throws Exception {
        this.route = new Route();
        this.factory = context.getBean(ExecutorFactory.class);
        this.registryConfig = context.getBean(RegistryConfig.class);

        initService();
        initProcessor();
    }

    @Override
    public void start() throws Exception {
        this.storeRegistry.start();
        this.idleScanner.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.storeRegistry.shutdown();
        this.idleScanner.shutdown();
    }

    private void initService() {
        HaClient haClient = context.getBean(HaClient.class);
        this.storeRegistry = new StoreRegistry(registryConfig, route, haClient);

        this.idleScanner = new IdleScanner(registryConfig, storeRegistry, route);
        this.clusterService = new ClusterService(registryConfig, route);
        this.topicService = new RegistryTopicService(registryConfig, route);

        ChannelCloser channelCloser = new ChannelCloser(storeRegistry, route);
        context.register(channelCloser);
    }

    private void initProcessor() {
        initRegistryProcessor();
        initRouteProcessor();
        initClusterProcessor();
        initTopicProcessor();
    }

    private void initRegistryProcessor() {
        KVService kvService = context.getBean(KVService.class);
        RegistryProcessor registryProcessor = new RegistryProcessor(registryConfig, storeRegistry, kvService, factory.getDefaultExecutor());
        context.register(registryProcessor);
    }

    private void initRouteProcessor() {
        KVService kvService = context.getBean(KVService.class);
        RouteProcessor routeProcessor = new RouteProcessor(registryConfig, topicService, kvService, factory.getRouteExecutor());
        context.register(routeProcessor);
    }

    private void initClusterProcessor() {
        ClusterProcessor clusterProcessor = new ClusterProcessor(clusterService, factory.getDefaultExecutor());
        context.register(clusterProcessor);
    }

    private void initTopicProcessor() {
        TopicProcessor topicProcessor = new TopicProcessor(registryConfig, topicService, factory.getDefaultExecutor());
        context.register(topicProcessor);
    }

}
