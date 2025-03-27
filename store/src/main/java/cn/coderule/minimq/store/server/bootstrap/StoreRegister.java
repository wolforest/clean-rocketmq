package cn.coderule.minimq.store.server.bootstrap;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicTable;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.store.server.StoreContext;
import java.util.List;

public class StoreRegister implements Lifecycle {
    private RegistryClient registryClient;
    private StoreConfig storeConfig;

    @Override
    public void initialize() {
        StoreContext.register(this);
    }

    @Override
    public void start() {
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

    @Override
    public void shutdown() {
        ServerInfo serverInfo = ServerInfo.builder()
                .clusterName(storeConfig.getCluster())
                .groupName(storeConfig.getGroup())
                .groupNo(storeConfig.getGroupNo())
                .address(storeConfig.getHost() + ":" + storeConfig.getPort())
                .build();

        registryClient.unregisterStore(serverInfo);
    }

    public void registerStore(TopicTable topicTable) {}
    public void unregisterStore() {}

    public void registerTopic(Topic topic) {
        TopicInfo topicInfo = TopicInfo.builder()
                .groupName(storeConfig.getGroup())
                .topic(topic)
                .build();

        registryClient.registerTopic(topicInfo);
    }


}
