package com.wolfmq.rpc.registry;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.rpc.registry.protocol.body.RegisterStoreResult;
import com.wolfmq.common.server.registry.BrokerInfo;
import com.wolfmq.common.server.registry.ClusterInfo;
import com.wolfmq.common.server.registry.GroupInfo;
import com.wolfmq.common.server.registry.heartbeat.HeartBeat;
import com.wolfmq.common.server.registry.ServerInfo;
import com.wolfmq.common.server.registry.StoreInfo;
import com.wolfmq.common.server.registry.route.RouteInfo;
import com.wolfmq.common.server.registry.route.TopicInfo;
import java.util.List;

public interface RegistryClient extends Lifecycle {
    List<String> getRegistryList();
    void setRegistryList(List<String> addressList);
    void setRegistryList(String addressConfig);
    String chooseRegistry() throws InterruptedException;

    void registerBroker(BrokerInfo brokerInfo);
    void unregisterBroker(ServerInfo serverInfo);
    void brokerHeartbeat(HeartBeat heartBeat);

    List<RegisterStoreResult> registerStore(StoreInfo storeInfo);
    void unregisterStore(ServerInfo serverInfo);
    void storeHeartbeat(HeartBeat heartBeat);
    void registerTopic(TopicInfo topicInfo);

    ClusterInfo syncClusterInfo(String clusterName) throws Exception;
    GroupInfo syncGroupInfo(String clusterName, String groupName) throws Exception;
    RouteInfo syncRouteInfo(String topicName, long timeout) throws Exception;
}
