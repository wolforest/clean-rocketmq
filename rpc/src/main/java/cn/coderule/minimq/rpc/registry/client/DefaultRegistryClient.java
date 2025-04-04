package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.rpc.registry.service.RegistryManager;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRegistryClient implements RegistryClient, Lifecycle {
    private final RpcClientConfig config;
    private final NettyClient nettyClient;
    private final RegistryManager registryManager;

    private final Lock channelLock;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
        this.config = config;
        this.nettyClient = new NettyClient(config);

        registryManager = new RegistryManager(config, addressConfig, nettyClient);
        this.channelLock = new ReentrantLock();
    }

    @Override
    public void start() {
        this.nettyClient.start();
    }

    @Override
    public void shutdown() {
        try {
            this.nettyClient.shutdown();
            this.registryManager.shutdown();
        } catch (Exception e) {
            log.error("shutdown client error", e);
        }
    }

    @Override
    public void setRegistryList(List<String> addrs) {
        registryManager.setRegistryList(addrs);
    }

    @Override
    public void setRegistryList(String addressConfig) {
        registryManager.setRegistryList(addressConfig);
    }

    @Override
    public List<String> getRegistryList() {
        return registryManager.getRegistryList();
    }

    @Override
    public void registerBroker(BrokerInfo brokerInfo) {
        List<StoreRegisterResult> results = new CopyOnWriteArrayList<>();


        RegisterBrokerRequestHeader requestHeader = new RegisterBrokerRequestHeader();
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

}
