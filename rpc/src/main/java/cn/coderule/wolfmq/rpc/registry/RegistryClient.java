package cn.coderule.wolfmq.rpc.registry;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.wolfmq.domain.server.registry.BrokerInfo;
import cn.coderule.wolfmq.domain.server.registry.ClusterInfo;
import cn.coderule.wolfmq.domain.server.registry.GroupInfo;
import cn.coderule.wolfmq.domain.server.heartbeat.HeartBeat;
import cn.coderule.wolfmq.domain.server.registry.ServerInfo;
import cn.coderule.wolfmq.domain.server.registry.StoreInfo;
import cn.coderule.wolfmq.domain.server.route.RouteInfo;
import cn.coderule.wolfmq.domain.server.route.TopicInfo;
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
