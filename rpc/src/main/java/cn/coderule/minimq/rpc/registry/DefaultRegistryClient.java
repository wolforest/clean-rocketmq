package cn.coderule.minimq.rpc.registry;

import cn.coderule.common.convention.service.Lifecycle;
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

public class DefaultRegistryClient implements RegistryClient, Lifecycle {

    @Override
    public List<String> getRegistryList() {
        return List.of();
    }

    @Override
    public void setRegistryList(List<String> serverList) {

    }

    @Override
    public void setRegistryString(String registryString) {

    }

    @Override
    public void setRegistryDomain(String domain) {

    }

    @Override
    public void scanRegistry() {

    }

    @Override
    public void registerBroker(BrokerInfo brokerInfo) {

    }

    @Override
    public void unregisterBroker(ServerInfo serverInfo) {

    }

    @Override
    public void brokerHeartbeat(HeartBeat heartBeat) {

    }

    @Override
    public void registerStore(StoreInfo storeInfo) {

    }

    @Override
    public void unregisterStore(ServerInfo serverInfo) {

    }

    @Override
    public void storeHeartbeat(HeartBeat heartBeat) {

    }

    @Override
    public void registerTopic(Topic topic, DataVersion version) {

    }

    @Override
    public ClusterInfo syncClusterInfo(String clusterName) {
        return null;
    }

    @Override
    public GroupInfo syncGroupInfo(String clusterName, String groupName) {
        return null;
    }

    @Override
    public RouteInfo syncRouteInfo(String topicName, long timeout) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
