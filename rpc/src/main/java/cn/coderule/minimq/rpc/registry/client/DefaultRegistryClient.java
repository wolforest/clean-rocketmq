package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.exception.MQException;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.codec.RpcSerializable;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.KVTable;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterBrokerBody;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigAndMappingSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.BrokerHeartbeatRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.QueryDataVersionRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterTopicRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import cn.coderule.minimq.rpc.registry.service.RegistryManager;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRegistryClient implements RegistryClient, Lifecycle {
    public static final int DEFAULT_RPC_TIMEOUT = 3_000;

    private final RpcClientConfig config;
    private final NettyClient nettyClient;

    private final RegistryManager registryManager;
    private final ExecutorService registerExecutor;

    private final StoreRegistryClient storeRegister;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
        this.config = config;
        this.nettyClient = new NettyClient(config);

        registryManager = new RegistryManager(config, addressConfig, nettyClient);
        registerExecutor = initRegisterExecutor();

        storeRegister = new StoreRegistryClient(registryManager, nettyClient, registerExecutor);
    }

    @Override
    public void start() {
        this.nettyClient.start();
        this.registryManager.start();
    }

    @Override
    public void shutdown() {
        try {
            this.nettyClient.shutdown();
            this.registryManager.shutdown();
            this.registerExecutor.shutdown();
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
    public String chooseRegistry() throws InterruptedException {
        return registryManager.chooseRegistry();
    }

    @Override
    public List<String> getRegistryList() {
        return registryManager.getRegistryList();
    }

    @Override
    public List<RegisterStoreResult>  registerStore(StoreInfo storeInfo) {
        return storeRegister.registerStore(storeInfo);
    }

    @Override
    public void unregisterStore(ServerInfo serverInfo) {
        storeRegister.unregisterStore(serverInfo);
    }

    @Override
    public void storeHeartbeat(HeartBeat heartBeat) {
        storeRegister.storeHeartbeat(heartBeat);
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
    public void registerTopic(TopicInfo topicInfo) {
        storeRegister.registerTopic(topicInfo);
    }

    @Override
    public ClusterInfo syncClusterInfo(String clusterName) {
        try {
            String registryAddress = registryManager.chooseRegistry();
        } catch (Exception e) {
            log.error("sync cluster info error", e);
        }

        return null;
    }

    @Override
    public GroupInfo syncGroupInfo(String clusterName, String groupName) {
        try {
            String registryAddress = registryManager.chooseRegistry();
        } catch (Exception e) {
            log.error("sync group info error", e);
        }

        return null;
    }

    @Override
    public RouteInfo syncRouteInfo(String topicName, long timeout) {
        try {
            String registryAddress = registryManager.chooseRegistry();
        } catch (Exception e) {
            log.error("sync route info error", e);
        }
        return null;
    }

    private ExecutorService initRegisterExecutor() {
        return ThreadUtil.newThreadPoolExecutor(
            4,
            10,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(32),
            new DefaultThreadFactory("ServerRegisterThread")
        );
    }

}
