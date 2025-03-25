package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.service.ClusterService;
import cn.coderule.minimq.registry.domain.store.service.IdleScanner;
import cn.coderule.minimq.registry.domain.store.service.TopicService;
import cn.coderule.minimq.registry.processor.ClusterProcessor;
import cn.coderule.minimq.registry.processor.RegistryProcessor;
import cn.coderule.minimq.registry.processor.RouteProcessor;
import cn.coderule.minimq.registry.processor.TopicProcessor;
import cn.coderule.minimq.registry.server.bootstrap.ExecutorFactory;
import cn.coderule.minimq.registry.server.RegistryContext;
import cn.coderule.minimq.registry.server.rpc.HaClient;

public class StoreManager implements Lifecycle {
    private RegistryConfig registryConfig;
    private Route route ;
    private ExecutorFactory factory;

    private StoreRegistry storeRegistry;
    private IdleScanner idleScanner;
    private ClusterService clusterService;
    private TopicService topicService;

    @Override
    public void initialize() {
        this.route = new Route();
        this.factory = RegistryContext.getBean(ExecutorFactory.class);
        this.registryConfig = RegistryContext.getBean(RegistryConfig.class);

        initService();
        initProcessor();
    }

    @Override
    public void start() {
        this.storeRegistry.start();
        this.idleScanner.start();
    }

    @Override
    public void shutdown() {
        this.storeRegistry.shutdown();
        this.idleScanner.shutdown();
    }

    private void initService() {
        HaClient haClient = RegistryContext.getBean(HaClient.class);
        this.storeRegistry = new StoreRegistry(registryConfig, route, haClient);

        this.idleScanner = new IdleScanner(registryConfig, storeRegistry, route);
        this.clusterService = new ClusterService(registryConfig, route);
        this.topicService = new TopicService(registryConfig, route);
    }

    private void initProcessor() {
        initRegistryProcessor();
        initRouteProcessor();
        initClusterProcessor();
        initTopicProcessor();
    }

    private void initRegistryProcessor() {
        KVService kvService = RegistryContext.getBean(KVService.class);
        RegistryProcessor registryProcessor = new RegistryProcessor(registryConfig, storeRegistry, kvService, factory.getDefaultExecutor());
        RegistryContext.register(registryProcessor);
    }

    private void initRouteProcessor() {
        KVService kvService = RegistryContext.getBean(KVService.class);
        RouteProcessor routeProcessor = new RouteProcessor(registryConfig, topicService, kvService, factory.getRouteExecutor());
        RegistryContext.register(routeProcessor);
    }

    private void initClusterProcessor() {
        ClusterProcessor clusterProcessor = new ClusterProcessor(clusterService, factory.getDefaultExecutor());
        RegistryContext.register(clusterProcessor);
    }

    private void initTopicProcessor() {
        TopicProcessor topicProcessor = new TopicProcessor(registryConfig, topicService, factory.getDefaultExecutor());
        RegistryContext.register(topicProcessor);
    }

}
