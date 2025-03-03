package cn.coderule.minimq.rpc.registry;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import java.util.List;

public interface RegistryClient {
    void setRpcClient(RpcClient rpcClient);

    List<String> getServerList();
    void setServerList(List<String> serverList);
    void scanServer();

    void registerBroker();
    void unregisterBroker();
    void brokerHeartbeat();

    void registerStore();
    void unregisterStore();
    void storeHeartbeat();
    void registerTopic(Topic topic, DataVersion version);

    GroupInfo syncGroupInfo(String clusterName, String groupName);
    ClusterInfo syncClusterInfo(String clusterName);

}
