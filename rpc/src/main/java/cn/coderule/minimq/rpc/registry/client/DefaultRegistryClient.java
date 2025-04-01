package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultRegistryClient implements RegistryClient, Lifecycle {
    private AtomicReference<List<String>> addressList;
    private String addressConfig;

    public DefaultRegistryClient(String addressConfig) {
        this.addressConfig = addressConfig;
        this.addressList = new AtomicReference<>();

        setRegistryList(addressConfig);
    }

    @Override
    public void setRegistryList(List<String> addressList) {
        if (CollectionUtil.isEmpty(addressList)) {
            return;
        }


    }

    @Override
    public void setRegistryList(String addressConfig) {
        if (StringUtil.isBlank(addressConfig)) {
            return;
        }

        String[] arr = addressConfig.split(";");
        if (arr.length == 0) {
            return;
        }
        this.addressConfig = addressConfig;
        setRegistryList(List.of(arr));
    }

    @Override
    public List<String> getRegistryList() {
        return List.of();
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
    public void registerTopic(TopicInfo topicInfo) {

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
