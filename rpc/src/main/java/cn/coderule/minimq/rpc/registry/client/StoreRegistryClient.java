package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.core.exception.RpcException;
import cn.coderule.minimq.domain.domain.meta.DataVersion;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.rpc.registry.protocol.body.KVTable;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterBrokerBody;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigAndMappingSerializeWrapper;
import cn.coderule.minimq.domain.domain.cluster.cluster.HeartBeat;
import cn.coderule.minimq.domain.domain.cluster.cluster.ServerInfo;
import cn.coderule.minimq.domain.domain.cluster.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.BrokerHeartbeatRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.QueryDataVersionRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.QueryDataVersionResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterTopicRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.minimq.domain.domain.cluster.route.TopicInfo;
import cn.coderule.minimq.rpc.registry.service.RegistryManager;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreRegistryClient  {
    private final NettyClient nettyClient;

    private final RegistryManager registryManager;
    private final ExecutorService registerExecutor;

    public StoreRegistryClient(RegistryManager registryManager, NettyClient nettyClient, ExecutorService registerExecutor) {
        this.registryManager = registryManager;
        this.nettyClient = nettyClient;
        this.registerExecutor = registerExecutor;
    }

    public List<RegisterStoreResult>  registerStore(StoreInfo storeInfo) {
        List<RegisterStoreResult> results = new CopyOnWriteArrayList<>();
        if (!needRegister(storeInfo)) {
            return results;
        }

        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return results;
        }

        RpcCommand request = createRegisterStoreRequest(storeInfo);
        CountDownLatch latch = new CountDownLatch(registrySet.size());
        for (String addr : registrySet) {
            registerStoreByExecutor(addr, storeInfo, request, results, latch);
        }

        awaitLatch(latch, storeInfo.getRegisterTimeout());
        return results;
    }

    public void unregisterStore(ServerInfo serverInfo) {
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return ;
        }

        RpcCommand request = createUnregisterStoreRequest(serverInfo);
        for (String addr : registrySet) {
            unregisterStore(addr, request);
        }
    }

    public void storeHeartbeat(HeartBeat heartBeat) {
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return ;
        }

        RpcCommand request = createStoreHeartbeatRequest(heartBeat);
        for (String addr : registrySet) {
            storeHeartbeat(addr, request);
        }
    }

    public void registerTopic(TopicInfo topicInfo) {
        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return ;
        }

        RpcCommand request = createRegisterTopicRequest(topicInfo);
        CountDownLatch latch = new CountDownLatch(registrySet.size());
        for (String addr : registrySet) {
            registerTopic(addr, request, topicInfo.getRegisterTimeout(), latch);
        }

        awaitLatch(latch, topicInfo.getRegisterTimeout());
    }

    private RpcCommand createQueryDataVersionRequest(StoreInfo storeInfo) {
        QueryDataVersionRequestHeader requestHeader = new QueryDataVersionRequestHeader();
        requestHeader.setClusterName(storeInfo.getClusterName());
        requestHeader.setBrokerName(storeInfo.getGroupName());
        requestHeader.setBrokerId(storeInfo.getGroupNo());
        requestHeader.setBrokerAddr(storeInfo.getAddress());

        RpcCommand request = RpcCommand.createRequestCommand(
            RequestCode.QUERY_DATA_VERSION,
            requestHeader
        );

        byte[] body = RpcSerializable.encode(storeInfo.getTopicInfo().getDataVersion());
        request.setBody(body);

        return request;
    }

    private boolean hasRegistryChanged(List<Boolean> changedList) {
        boolean changed = false;
        for (Boolean status : changedList) {
            if (!status) {
                continue;
            }

            changed = true;
            break;
        }

        return changed;
    }

    private boolean isTopicInfoChanged(StoreInfo storeInfo, RpcCommand response) throws RemotingCommandException {
        boolean changed = false;
        if (!response.isSuccess()) {
            return false;
        }

        QueryDataVersionResponseHeader responseHeader = response.decodeHeader(QueryDataVersionResponseHeader.class);
        if (null != responseHeader.getChanged() && responseHeader.getChanged()) {
            changed = true;
        }

        byte[] body = response.getBody();
        DataVersion requestVersion = storeInfo.getTopicInfo().getDataVersion();
        DataVersion responseVersion = RpcSerializable.decode(body, DataVersion.class);
        if (!requestVersion.equals(responseVersion)) {
            changed = true;
        }

        return changed;
    }

    private void queryRegistryVersion(String registryAddress, RpcCommand request, StoreInfo storeInfo, List<Boolean> changedList, CountDownLatch latch) {
        registerExecutor.execute(() -> {
            try {
                RpcCommand response = nettyClient.invokeSync(registryAddress, request, storeInfo.getRegisterTimeout());
                if (isTopicInfoChanged(storeInfo, response)) {
                    changedList.add(true);
                }
            } catch (Exception e) {
                changedList.add(true);
                log.error("query registry version error, registry address: {}", registryAddress, e);
            } finally {
                latch.countDown();
            }
        });
    }

    private boolean needRegister(StoreInfo storeInfo) {
        if (storeInfo.isForceRegister()) {
            return true;
        }

        Set<String> registrySet = registryManager.getAvailableRegistry();
        if (CollectionUtil.isEmpty(registrySet)) {
            return false;
        }

        List<Boolean> changedList = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(registrySet.size());
        RpcCommand request = createQueryDataVersionRequest(storeInfo);
        for (String registryAddr : registrySet) {
            queryRegistryVersion(registryAddr, request, storeInfo, changedList, latch);
        }

        awaitLatch(latch, storeInfo.getRegisterTimeout());
        return hasRegistryChanged(changedList);
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
        if (storeInfo.isOneway()) {
            return invokeOneway(registryAddress, storeInfo, request);
        }

        RpcCommand response = nettyClient.invokeSync(registryAddress, request, storeInfo.getRegisterTimeout());
        assert response != null;

        RegisterBrokerRequestHeader requestHeader = (RegisterBrokerRequestHeader) request.readCustomHeader();
        if (!response.isSuccess()) {
            throw new RpcException(response.getCode(), response.getRemark(), requestHeader.getBrokerAddr());
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

    private void awaitLatch(CountDownLatch latch, long timeout) {
        try {
            boolean status = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!status) {
                log.warn("register store timeout, {}ms", timeout);
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

    private void unregisterStore(String registryAddress, RpcCommand request) {
        try {

            RpcCommand response = nettyClient.invokeSync(registryAddress, request, DefaultRegistryClient.DEFAULT_RPC_TIMEOUT);

            assert response != null;
            if (!response.isSuccess()) {
                throw new RpcException(response.getCode(), "unregister store error, registry address: " + registryAddress);
            }

        } catch (Exception e) {
            log.warn("unregister store error, registry address: {}", registryAddress, e);
        }
    }

    private RpcCommand createQueryDataVersionRequest(HeartBeat heartBeat) {
        QueryDataVersionRequestHeader requestHeader = new QueryDataVersionRequestHeader();
        requestHeader.setClusterName(heartBeat.getClusterName());
        requestHeader.setBrokerName(heartBeat.getGroupName());
        requestHeader.setBrokerId(heartBeat.getGroupNo());
        requestHeader.setBrokerAddr(heartBeat.getAddress());

        RpcCommand request = RpcCommand.createRequestCommand(
            RequestCode.QUERY_DATA_VERSION,
            requestHeader
        );

        byte[] body = RpcSerializable.encode(heartBeat.getVersion());
        request.setBody(body);

        return request;
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

    private void storeHeartbeat(String registryAddress, RpcCommand request) {
        registerExecutor.execute(() -> {
            try {
                nettyClient.invokeOneway(registryAddress, request, DefaultRegistryClient.DEFAULT_RPC_TIMEOUT);
            } catch (Exception e) {
                log.error("store heartbeat error, registry address: {}", registryAddress, e);
            }
        });
    }

    private void registerTopic(String registryAddress, RpcCommand request, int timeout, CountDownLatch latch) {
        registerExecutor.execute(() -> {
            try {
                RpcCommand response = nettyClient.invokeSync(registryAddress, request, timeout);
                assert response != null;
                log.info("register topic success, registry address: {}, response code: {}",
                    registryAddress, response.getCode());
            } catch (Exception e) {
                log.warn("register topic error, registry address: {}", registryAddress, e);
            } finally {
                latch.countDown();
            }
        });
    }

    private RpcCommand createRegisterTopicRequest(TopicInfo topicInfo) {
        RegisterTopicRequestHeader requestHeader = new RegisterTopicRequestHeader();

        String topicName = topicInfo.getTopic().getTopicName();
        requestHeader.setTopic(topicName);

        RpcCommand request = RpcCommand.createRequestCommand(
            RequestCode.REGISTER_TOPIC_IN_NAMESRV,
            requestHeader
        );

        RouteInfo routeInfo = topicInfo.toRouteInfo();
        request.setBody(routeInfo.encode());

        return request;
    }

}
