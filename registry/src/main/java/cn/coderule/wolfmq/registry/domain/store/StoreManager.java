package cn.coderule.wolfmq.registry.domain.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.RegistryConfig;
import cn.coderule.wolfmq.registry.domain.kv.KVService;
import cn.coderule.wolfmq.registry.domain.store.model.Route;
import cn.coderule.wolfmq.registry.domain.store.service.ChannelCloser;
import cn.coderule.wolfmq.registry.domain.store.service.ClusterService;
import cn.coderule.wolfmq.registry.domain.store.service.IdleScanner;
import cn.coderule.wolfmq.registry.domain.store.service.RegistryTopicService;
import cn.coderule.wolfmq.registry.processor.ClusterProcessor;
import cn.coderule.wolfmq.registry.processor.RegistryProcessor;
import cn.coderule.wolfmq.registry.processor.RouteProcessor;
import cn.coderule.wolfmq.registry.processor.TopicProcessor;
import cn.coderule.wolfmq.registry.server.bootstrap.ExecutorFactory;
import cn.coderule.wolfmq.registry.server.bootstrap.RegistryContext;
import cn.coderule.wolfmq.registry.server.rpc.HaClient;

public class StoreManager implements Lifecycle {
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
        this.factory = RegistryContext.getBean(ExecutorFactory.class);
        this.registryConfig = RegistryContext.getBean(RegistryConfig.class);

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
        HaClient haClient = RegistryContext.getBean(HaClient.class);
        this.storeRegistry = new StoreRegistry(registryConfig, route, haClient);

        this.idleScanner = new IdleScanner(registryConfig, storeRegistry, route);
        this.clusterService = new ClusterService(registryConfig, route);
        this.topicService = new RegistryTopicService(registryConfig, route);

        ChannelCloser channelCloser = new ChannelCloser(storeRegistry, route);
        RegistryContext.register(channelCloser);
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
