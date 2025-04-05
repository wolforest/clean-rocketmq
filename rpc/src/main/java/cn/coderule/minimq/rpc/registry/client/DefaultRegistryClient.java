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
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerResponseHeader;
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
    private static final int DEFAULT_RPC_TIMEOUT = 3_000;

    private final RpcClientConfig config;
    private final NettyClient nettyClient;

    private final RegistryManager registryManager;
    private final ExecutorService registerExecutor;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
        this.config = config;
        this.nettyClient = new NettyClient(config);

        registryManager = new RegistryManager(config, addressConfig, nettyClient);
        registerExecutor = initRegisterExecutor();
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
        List<RegisterStoreResult> results = new CopyOnWriteArrayList<>();
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return results;
        }

        RpcCommand request = createRegisterStoreRequest(storeInfo);
        CountDownLatch latch = new CountDownLatch(registrySet.size());
        for (String addr : registrySet) {
            registerStoreByExecutor(addr, storeInfo, request, results, latch);
        }

        awaitLatch(latch, storeInfo);
        return results;
    }


    @Override
    public void unregisterStore(ServerInfo serverInfo) {
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return ;
        }

        for (String addr : registrySet) {
            unregisterStore(addr, serverInfo);
        }
    }

    private RpcCommand createQueryDataVersionRequest(HeartBeat heartBeat) {


        return null;
    }

    private RpcCommand createStoreHeartbeatRequest(HeartBeat heartBeat) {
        if (null != heartBeat.getVersion()) {
            return createQueryDataVersionRequest(heartBeat);
        }

        BrokerHeartbeatRequestHeader requestHeader = new BrokerHeartbeatRequestHeader();
        requestHeader.setClusterName(heartBeat.getClusterName());
        requestHeader.setBrokerAddr(heartBeat.getAddress());
        requestHeader.setBrokerName(heartBeat.getGroupName());

        return RpcCommand.createRequestCommand(
            RequestCode.BROKER_HEARTBEAT,
            requestHeader
        );
    }

    private void storeHeartbeat(String registryAddress, HeartBeat heartBeat) {

    }

    @Override
    public void storeHeartbeat(HeartBeat heartBeat) {
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return ;
        }

        for (String addr : registrySet) {
            storeHeartbeat(addr, heartBeat);
        }
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

    private RegisterBrokerRequestHeader createRegisterStoreRequestHeader(StoreInfo storeInfo) {
        RegisterBrokerRequestHeader requestHeader = new RegisterBrokerRequestHeader();
        requestHeader.setBrokerAddr(storeInfo.getAddress());
        requestHeader.setBrokerName(storeInfo.getGroupName());
        requestHeader.setBrokerId(storeInfo.getGroupNo());
        requestHeader.setClusterName(storeInfo.getClusterName());
        requestHeader.setHaServerAddr(storeInfo.getHaAddress());
        requestHeader.setHeartbeatTimeoutMillis(storeInfo.getHeartbeatTimeout().longValue());
        requestHeader.setEnableActingMaster(storeInfo.isEnableMasterElection());

        return requestHeader;
    }

    private RegisterBrokerBody createRegisterStoreRequestBody(StoreInfo storeInfo) {
        RegisterBrokerBody requestBody = new RegisterBrokerBody();
        requestBody.setFilterServerList(storeInfo.getFilterList());

        requestBody.setTopicConfigSerializeWrapper(
            TopicConfigAndMappingSerializeWrapper.from(storeInfo.getTopicInfo())
        );

        return requestBody;
    }

    private RpcCommand createRegisterStoreRequest(StoreInfo storeInfo) {
        RegisterBrokerRequestHeader requestHeader = createRegisterStoreRequestHeader(storeInfo);
        RegisterBrokerBody requestBody = createRegisterStoreRequestBody(storeInfo);

        byte[] body = requestBody.encode(storeInfo.isCompressed());
        int crc32 = HashUtil.crc32(body);
        requestHeader.setBodyCrc32(crc32);

        RpcCommand request = RpcCommand.createRequestCommand(RequestCode.REGISTER_BROKER, requestHeader);
        request.setBody(body);

        return request;
    }

    private RegisterStoreResult toRegisterStoreResult(RpcCommand response) throws RemotingCommandException {
        RegisterBrokerResponseHeader responseHeader = response.decodeHeader(RegisterBrokerResponseHeader.class);

        RegisterStoreResult result = new RegisterStoreResult();
        result.setMasterAddr(responseHeader.getMasterAddr());
        result.setHaServerAddr(responseHeader.getHaServerAddr());

        if (null != response.getBody()) {
            result.setKvTable(KVTable.decode(response.getBody(), KVTable.class));
        }

        return result;
    }

    private RegisterStoreResult invokeOneway(String registryAddress, StoreInfo storeInfo, RpcCommand request) {
        try {
            nettyClient.invokeOneway(registryAddress, request, storeInfo.getRegisterTimeout());
        } catch (Exception ignore) {
        }

        return null;
    }

    private RegisterStoreResult invoke(String registryAddress, StoreInfo storeInfo, RpcCommand request) throws Exception {
        if (request.isOnewayRPC()) {
            return invokeOneway(registryAddress, storeInfo, request);
        }

        RpcCommand response = nettyClient.invokeSync(registryAddress, request, storeInfo.getRegisterTimeout());
        assert response != null;

        RegisterBrokerRequestHeader requestHeader = (RegisterBrokerRequestHeader) request.readCustomHeader();
        if (!response.isSuccess()) {
            throw new MQException(response.getCode(), response.getRemark(), requestHeader.getBrokerAddr());
        }

        return toRegisterStoreResult(response);
    }

    private void registerStoreByExecutor(String registryAddress, StoreInfo storeInfo, RpcCommand request, List<RegisterStoreResult> results, CountDownLatch latch) {
        registerExecutor.execute(() -> {
            try {
                RegisterStoreResult result = invoke(registryAddress, storeInfo, request);
                if (result != null) {
                    results.add(result);
                }
                log.info("register store success, registry address: {}, result: {}", registryAddress, result);
            } catch (Exception e) {
                log.error("register store error, registry address: {}", registryAddress, e);
            } finally {
                latch.countDown();
            }
        });
    }

    private void awaitLatch(CountDownLatch latch, StoreInfo storeInfo) {
        try {
            boolean status = latch.await(storeInfo.getRegisterTimeout(), TimeUnit.MILLISECONDS);
            if (!status) {
                log.warn("register store timeout, {}ms", storeInfo.getRegisterTimeout());
            }
        } catch (InterruptedException ignore) {
        }
    }

    private RpcCommand createUnregisterStoreRequest(ServerInfo serverInfo) {
        UnRegisterBrokerRequestHeader requestHeader = new UnRegisterBrokerRequestHeader();
        requestHeader.setClusterName(serverInfo.getClusterName());
        requestHeader.setBrokerName(serverInfo.getGroupName());
        requestHeader.setBrokerId(serverInfo.getGroupNo());
        requestHeader.setBrokerAddr(serverInfo.getAddress());

        return RpcCommand.createRequestCommand(
            RequestCode.UNREGISTER_BROKER, requestHeader
        );
    }

    private void unregisterStore(String registryAddress, ServerInfo serverInfo) {
        try {
            RpcCommand request = createUnregisterStoreRequest(serverInfo);
            RpcCommand response = nettyClient.invokeSync(registryAddress, request, DEFAULT_RPC_TIMEOUT);

            assert response != null;
            if (!response.isSuccess()) {
                throw new MQException(response.getCode(), "unregister store error, registry address: " + registryAddress);
            }

        } catch (Exception e) {
            log.warn("unregister store error, registry address: {}", registryAddress, e);
        }
    }

}
