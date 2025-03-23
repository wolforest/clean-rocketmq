package cn.coderule.minimq.rpc.registry;

import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import java.util.List;

public interface RegistryClient {
    List<String> getRegistryList();
    void setRegistryAddress(String address);

    void registerBroker(BrokerInfo brokerInfo);
    void unregisterBroker(ServerInfo serverInfo);
    void brokerHeartbeat(HeartBeat heartBeat);

    void registerStore(StoreInfo storeInfo);
    void unregisterStore(ServerInfo serverInfo);
    void storeHeartbeat(HeartBeat heartBeat);
    void registerTopic(TopicInfo topicInfo);

    ClusterInfo syncClusterInfo(String clusterName);
    GroupInfo syncGroupInfo(String clusterName, String groupName);
    RouteInfo syncRouteInfo(String topicName, long timeout);
}
