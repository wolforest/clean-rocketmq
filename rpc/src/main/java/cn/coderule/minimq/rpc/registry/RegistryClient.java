package cn.coderule.minimq.rpc.registry;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.List;

public interface RegistryClient {
    List<String> getRegistryList();
    void setRegistryList(List<String> serverList);
    void setRegistryString(String registryString);
    void setRegistryDomain(String  domain);
    void scanRegistry();

    void registerBroker(BrokerInfo brokerInfo);
    void unregisterBroker(ServerInfo serverInfo);
    void brokerHeartbeat(HeartBeat heartBeat);

    void registerStore(StoreInfo storeInfo);
    void unregisterStore(ServerInfo serverInfo);
    void storeHeartbeat(HeartBeat heartBeat);
    void registerTopic(Topic topic, DataVersion version);

    ClusterInfo syncClusterInfo(String clusterName);
    GroupInfo syncGroupInfo(String clusterName, String groupName);
    RouteInfo syncRouteInfo(String topicName, long timeout);
}
