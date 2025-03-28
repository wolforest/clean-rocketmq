package cn.coderule.minimq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicTable;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.store.server.StoreContext;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StoreRegister implements Lifecycle {
    private final StoreConfig storeConfig;
    private final RegistryClient registryClient;
    private final ScheduledExecutorService scheduler;

    public StoreRegister(StoreConfig storeConfig) {
        this.registryClient = new DefaultRegistryClient();
        this.storeConfig = storeConfig;

        scheduler = ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("StoreHeartbeatThread_")
        );
    }

    @Override
    public void initialize() {
        StoreContext.register(this);
    }

    @Override
    public void start() {
        registerStore();

        scheduler.scheduleAtFixedRate(
            this::heartbeat,
            1000,
            storeConfig.getRegistryHeartbeatInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() {
        unregisterStore();
        scheduler.shutdown();
    }

    private void registerStore() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        TopicTable topicTable = topicService.getTopicTable();

        TopicConfigSerializeWrapper topicInfo = new TopicConfigSerializeWrapper();
        topicInfo.setTopicConfigTable(topicTable.getTopicTable());
        topicInfo.setDataVersion(topicTable.getDataVersion());

        StoreInfo storeInfo = StoreInfo.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .haAddress(storeConfig.getHost() + ":" + storeConfig.getHaPort())
            .topicInfo(topicInfo)
            .filterList(List.of())
            .build();

        registryClient.registerStore(storeInfo);
    }

    private void unregisterStore() {
        ServerInfo serverInfo = ServerInfo.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .build();

        registryClient.unregisterStore(serverInfo);
    }

    private void heartbeat() {

    }

    public void registerTopic(Topic topic) {
        TopicInfo topicInfo = TopicInfo.builder()
                .groupName(storeConfig.getGroup())
                .topic(topic)
                .build();

        registryClient.registerTopic(topicInfo);
    }


}
