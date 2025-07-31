package cn.coderule.minimq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.constant.PermName;
import cn.coderule.minimq.domain.domain.meta.DataVersion;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.meta.topic.TopicMap;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.config.network.RpcClientConfig;
import cn.coderule.minimq.domain.core.enums.RequestType;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.domain.domain.meta.topic.TopicConfigSerializeWrapper;
import cn.coderule.minimq.domain.domain.cluster.cluster.HeartBeat;
import cn.coderule.minimq.domain.domain.cluster.cluster.ServerInfo;
import cn.coderule.minimq.domain.domain.cluster.cluster.StoreInfo;
import cn.coderule.minimq.domain.domain.cluster.route.TopicInfo;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class StoreRegister implements Lifecycle {
    private final StoreConfig storeConfig;
    private final RegistryClient registryClient;

    public StoreRegister(StoreConfig storeConfig) {
        this.registryClient = new DefaultRegistryClient(
            new RpcClientConfig(),
            storeConfig.getRegistryAddress()
        );
        this.storeConfig = storeConfig;
    }

    @Override
    public void start() throws Exception {
        this.registryClient.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.registryClient.shutdown();
    }

    public void registerTopic(Topic topic) {
        Topic registerTopic = mergeServerPermission(topic);
        TopicInfo topicInfo = createTopicInfo(registerTopic);

        registryClient.registerTopic(topicInfo);
    }

    public void registerStore(boolean updateOrderConfig, boolean oneway, boolean forceRegister) {
        StoreInfo storeInfo = createStoreInfo(oneway, forceRegister);
        List<RegisterStoreResult> results = registryClient.registerStore(storeInfo);
        updateClusterInfo(results, updateOrderConfig);

        log.info("register store, request: {}; response: {}", storeInfo, results);
    }

    public void unregisterStore() {
        ServerInfo serverInfo = ServerInfo.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .build();

        registryClient.unregisterStore(serverInfo);
    }

    public void heartbeat() {
        if (!storeConfig.isEnableRegistryHeartbeat()) {
            return;
        }

        try {
            HeartBeat heartBeat = createHeartBeat();
            registryClient.storeHeartbeat(heartBeat);
        } catch (Exception e) {
            log.error("store registry heartbeat error", e);
        }
    }

    private TopicConfigSerializeWrapper getTopicInfo() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        TopicMap topicMap = topicService.getTopicMap();

        TopicConfigSerializeWrapper topicInfo = new TopicConfigSerializeWrapper();
        topicInfo.setTopicConfigTable(topicMap.getTopicTable());
        topicInfo.setDataVersion(topicMap.getVersion());

        return topicInfo;
    }

    private StoreInfo createStoreInfo(boolean oneway, boolean forceRegister) {
        return StoreInfo.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .haAddress(storeConfig.getHost() + ":" + storeConfig.getHaPort())

            .registerTimeout(storeConfig.getRegistryTimeout())
            .heartbeatTimeout(storeConfig.getRegistryHeartbeatTimeout())
            .heartbeatInterval(storeConfig.getRegistryHeartbeatInterval())
            .enableMasterElection(storeConfig.isEnableMasterElection())
            .registerType(oneway ? RequestType.ONEWAY : RequestType.SYNC)
            .forceRegister(forceRegister)

            .topicInfo(getTopicInfo())
            .filterList(List.of())
            .build();
    }

    private void updateClusterInfo(List<RegisterStoreResult> results, boolean updateOrderConfig) {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        for (RegisterStoreResult result : results) {
            if (result == null) {
                continue;
            }

            if (shouldUpdateMasterAddress(result)) {
                storeConfig.setMasterAddress(result.getMasterAddr());
            }

            if (shouldUpdateHaAddress(result)) {
                storeConfig.setHaAddress(result.getHaServerAddr());
            }

            if (shouldUpdateOrderConfig(result, updateOrderConfig)) {
                topicService.updateOrderConfig(result.getKvTable().getTable());
            }
        }
    }

    private boolean shouldUpdateOrderConfig(RegisterStoreResult result, boolean updateOrderConfig) {
        return updateOrderConfig
            && null != result.getKvTable()
            && MapUtil.notEmpty(result.getKvTable().getTable());
    }

    private boolean shouldUpdateMasterAddress(RegisterStoreResult result) {
        return storeConfig.isRefreshMasterAddress()
            && StringUtil.notBlank(result.getMasterAddr())
            && !result.getMasterAddr().equals(storeConfig.getMasterAddress());
    }

    private boolean shouldUpdateHaAddress(RegisterStoreResult result) {
        return storeConfig.isRefreshHaAddress()
            && StringUtil.notBlank(result.getHaServerAddr())
            && !result.getHaServerAddr().equals(storeConfig.getHaAddress());
    }

    private HeartBeat createHeartBeat() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        DataVersion version = topicService.getTopicMap().getVersion();

        return HeartBeat.builder()
            .clusterName(storeConfig.getCluster())
            .groupName(storeConfig.getGroup())
            .groupNo(storeConfig.getGroupNo())
            .address(storeConfig.getHost() + ":" + storeConfig.getPort())
            .heartbeatInterval(storeConfig.getRegistryHeartbeatInterval())
            .heartbeatTimeout(storeConfig.getRegistryHeartbeatTimeout())
            .inContainer(storeConfig.isInContainer())
            .version(version)
            .build();
    }

    private Topic mergeServerPermission(Topic topic) {
        Topic registerTopic = topic;
        if (!PermName.isWriteable(storeConfig.getPermission())
            || !PermName.isReadable(storeConfig.getPermission())) {
            registerTopic = new Topic(topic);
            registerTopic.setPerm(topic.getPerm() & storeConfig.getPermission());
        }

        return registerTopic;
    }

    private TopicInfo createTopicInfo(Topic registerTopic) {
        return TopicInfo.builder()
            .groupName(storeConfig.getGroup())
            .topic(registerTopic)
            .build();
    }

}
