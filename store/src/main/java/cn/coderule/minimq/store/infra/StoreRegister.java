package cn.coderule.minimq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.constant.PermName;
import cn.coderule.minimq.domain.model.DataVersion;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicMap;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.store.server.StoreContext;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreRegister implements Lifecycle {
    private final StoreConfig storeConfig;
    private final RegistryClient registryClient;
    private final ScheduledExecutorService heartbeatScheduler;

    public StoreRegister(StoreConfig storeConfig) {
        this.registryClient = new DefaultRegistryClient(
            new RpcClientConfig(),
            storeConfig.getRegistryAddress()
        );
        this.storeConfig = storeConfig;

        heartbeatScheduler = ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("StoreHeartbeatThread_")
        );
    }

    @Override
    public void start() {
        registerStore();
        startHeartbeat();
    }

    @Override
    public void shutdown() {
        unregisterStore();
        heartbeatScheduler.shutdown();
    }

    public void registerTopic(Topic topic) {
        Topic registerTopic = mergeServerPermission(topic);
        TopicInfo topicInfo = createTopicInfo(registerTopic);

        registryClient.registerTopic(topicInfo);
    }

    private void registerStore() {
        StoreInfo storeInfo = createStoreInfo();
        List<RegisterStoreResult> results = registryClient.registerStore(storeInfo);
        updateClusterInfo(results);
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

    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(
            this::heartbeat,
            1000,
            storeConfig.getRegistryHeartbeatInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    private void heartbeat() {
        if (!storeConfig.isEnableMasterElection()) {
            return;
        }

        try {
            HeartBeat heartBeat = createHeartBeat();
            registryClient.storeHeartbeat(heartBeat);
        } catch (Exception e) {
            log.error("store registry heartbeat error", e);
        }
    }

    private StoreInfo createStoreInfo() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        TopicMap topicMap = topicService.getTopicMap();

        TopicConfigSerializeWrapper topicInfo = new TopicConfigSerializeWrapper();
        topicInfo.setTopicConfigTable(topicMap.getTopicTable());
        topicInfo.setDataVersion(topicMap.getVersion());

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
            .topicInfo(topicInfo)
            .filterList(List.of())
            .build();
    }

    private void updateClusterInfo(List<RegisterStoreResult> results) {
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

            if (shouldUpdateOrderConfig(result)) {
                topicService.updateOrderConfig(result.getKvTable().getTable());
            }
        }
    }

    private boolean shouldUpdateOrderConfig(RegisterStoreResult result) {
        return MapUtil.notEmpty(result.getKvTable().getTable());
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
