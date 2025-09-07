package cn.coderule.minimq.rpc.registry;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.domain.domain.cluster.server.BrokerInfo;
import cn.coderule.minimq.domain.domain.cluster.server.ClusterInfo;
import cn.coderule.minimq.domain.domain.cluster.server.GroupInfo;
import cn.coderule.minimq.domain.domain.cluster.server.HeartBeat;
import cn.coderule.minimq.domain.domain.cluster.server.ServerInfo;
import cn.coderule.minimq.domain.domain.cluster.server.StoreInfo;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.minimq.domain.domain.cluster.route.TopicInfo;
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
