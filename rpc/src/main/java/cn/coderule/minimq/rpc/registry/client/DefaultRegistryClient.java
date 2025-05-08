package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.domain.exception.RpcException;
import cn.coderule.minimq.rpc.common.rpc.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.GetBrokerMemberGroupResponseBody;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.BrokerInfo;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.ClusterInfo;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.HeartBeat;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.ServerInfo;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.GetBrokerMemberGroupRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.GetRouteInfoRequestHeader;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;
import cn.coderule.minimq.domain.domain.model.cluster.route.TopicInfo;
import cn.coderule.minimq.rpc.registry.service.RegistryManager;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRegistryClient implements RegistryClient {
    public static final int DEFAULT_RPC_TIMEOUT = 3_000;

    private final NettyClient nettyClient;
    private final RegistryManager registryManager;
    private final ExecutorService registerExecutor;

    private final StoreRegistryClient storeRegister;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
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
    public ClusterInfo syncClusterInfo(String clusterName) throws Exception {
        String registryAddress = registryManager.chooseRegistry();
        RpcCommand request = RpcCommand.createRequestCommand(RequestCode.QUERY_DATA_VERSION);

        RpcCommand response = nettyClient.invokeSync(registryAddress, request, DEFAULT_RPC_TIMEOUT);
        assert response != null;

        if (response.isSuccess()) {
            return RpcSerializable.decode(response.getBody(), ClusterInfo.class);
        }

        throw new RpcException(
            response.getCode(),
            "sync cluster info error, registry address: " + registryAddress
        );
    }

    @Override
    public GroupInfo syncGroupInfo(String clusterName, String groupName) throws Exception {
        String registryAddress = registryManager.chooseRegistry();
        RpcCommand request = createGroupQueryRequest(clusterName, groupName);

        RpcCommand response = nettyClient.invokeSync(registryAddress, request, DEFAULT_RPC_TIMEOUT);
        assert response != null;

        return formatGroupInfo(clusterName, groupName, response);
    }

    @Override
    public RouteInfo syncRouteInfo(String topicName, long timeout) throws Exception {
        String registryAddress = registryManager.chooseRegistry();
        RpcCommand request = createRouteQueryRequest(topicName);

        RpcCommand response = nettyClient.invokeSync(registryAddress, request, timeout);
        assert response != null;

        if (!response.isSuccess()) {
            throw new RpcException(response.getCode(), "query route info error, registry address: " + registryAddress);
        }

        byte[] body = response.getBody();
        if (body != null) {
            return RpcSerializable.decode(body, RouteInfo.class);
        }

        throw new RpcException(response.getCode(), "query route info error, registry address: " + registryAddress);
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

    private RpcCommand createRouteQueryRequest(String topicName) {
        GetRouteInfoRequestHeader requestHeader = new GetRouteInfoRequestHeader();
        requestHeader.setTopic(topicName);
        return RpcCommand.createRequestCommand(RequestCode.GET_ROUTEINFO_BY_TOPIC, requestHeader);
    }

    private RpcCommand createGroupQueryRequest(String clusterName, String groupName) {
        GetBrokerMemberGroupRequestHeader requestHeader = new GetBrokerMemberGroupRequestHeader();
        requestHeader.setClusterName(clusterName);
        requestHeader.setBrokerName(groupName);
        return RpcCommand.createRequestCommand(RequestCode.GET_BROKER_MEMBER_GROUP, requestHeader);
    }

    private GroupInfo formatGroupInfo(String clusterName, String groupName, RpcCommand response) {
        GroupInfo groupInfo = new GroupInfo(clusterName, groupName);
        if (!response.isSuccess()) {
            return groupInfo;
        }

        byte[] body = response.getBody();
        if (body == null) {
            return groupInfo;
        }

        GetBrokerMemberGroupResponseBody responseBody = GetBrokerMemberGroupResponseBody.decode(body, GetBrokerMemberGroupResponseBody.class);
        groupInfo.setBrokerAddrs(responseBody.getBrokerMemberGroup().getBrokerAddrs());
        return groupInfo;
    }


}
