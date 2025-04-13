package cn.coderule.minimq.registry.processor;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.domain.constant.MQConstants;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.store.StoreRegistry;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import cn.coderule.minimq.domain.domain.model.DataVersion;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterBrokerBody;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterTopicRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryProcessor implements RpcProcessor {
    private final RegistryConfig registryConfig;
    private final StoreRegistry storeRegistry;
    private final KVService kvService;

    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.REGISTER_BROKER,
        RequestCode.UNREGISTER_BROKER,
        RequestCode.REGISTER_TOPIC_IN_NAMESRV
    );

    public RegistryProcessor(RegistryConfig registryConfig, StoreRegistry storeRegistry, KVService kvService, ExecutorService executor) {
        this.registryConfig = registryConfig;
        this.storeRegistry = storeRegistry;
        this.kvService = kvService;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.REGISTER_BROKER -> registerStore(ctx, request);
            case RequestCode.UNREGISTER_BROKER -> unregisterStore(ctx, request);
            case RequestCode.REGISTER_TOPIC_IN_NAMESRV -> registerTopic(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand registerStore(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RegisterBrokerRequestHeader requestHeader = request.decodeHeader(RegisterBrokerRequestHeader.class);
        RpcCommand response = RpcCommand.createResponseCommand(RegisterBrokerResponseHeader.class);
        RegisterBrokerResponseHeader responseHeader = (RegisterBrokerResponseHeader) response.readCustomHeader();

        if (!checksum(ctx.getChannelContext(), request, requestHeader, response)) {
            return response;
        }

        RegisterStoreResult result = registerStore(ctx, request, requestHeader);
        if (result == null) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "register failed");
        }

        formatResponseHeader(result, responseHeader);
        addOrderConfig(response);

        return response.success();
    }

    private RpcCommand unregisterStore(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        UnRegisterBrokerRequestHeader requestHeader = request.decodeHeader(UnRegisterBrokerRequestHeader.class);

        boolean isSuccess = storeRegistry.unregisterAsync(requestHeader);
        if (!isSuccess) {
            log.warn("unregister store failed, request: {}", requestHeader);
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "unregister failed");
        }
        return response.success();
    }

    private RpcCommand registerTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        RegisterTopicRequestHeader requestHeader = request.decodeHeader(RegisterTopicRequestHeader.class);

        RouteInfo routeInfo = RouteInfo.decode(request.getBody());

        if (null ==  routeInfo || CollectionUtil.isEmpty(routeInfo.getQueueDatas())) {
            return response.success();
        }

        String groupName = getGroupName(routeInfo);
        List<Topic> topicList = toTopicList(requestHeader.getTopic(), routeInfo);
        storeRegistry.registerTopic(groupName, topicList);

        return response.success();
    }

    private String getGroupName(RouteInfo routeInfo) {
        QueueInfo first = routeInfo.getQueueDatas().get(0);
        if (first == null) {
            return null;
        }

        return first.getBrokerName();
    }

    private List<Topic> toTopicList(String topicName, RouteInfo routeInfo) {
        return routeInfo.getQueueDatas().stream()
            .map(queueData -> queueData.toTopic(topicName))
            .toList();
    }

    private boolean checksum(ChannelHandlerContext ctx, RpcCommand request, RegisterBrokerRequestHeader requestHeader, RpcCommand response) {
        if (requestHeader.getBodyCrc32() == 0) {
            return true;
        }

        final int crc32 = HashUtil.crc32(request.getBody());
        if (crc32 != requestHeader.getBodyCrc32()) {

            log.warn(String.format("receive registerBroker request,crc32 not match,from %s",
                NettyHelper.getRemoteAddr(ctx.channel())));

            response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "crc32 not match");
            return false;
        }

        return true;
    }

    private RegisterBrokerBody extractBody(RpcCommand request, RegisterBrokerRequestHeader requestHeader) throws RemotingCommandException {
        RegisterBrokerBody body = new RegisterBrokerBody();
        if (null == request.getBody()) {
            DataVersion version = body.getTopicConfigSerializeWrapper().getDataVersion();
            version.setCounter(new AtomicLong(0));
            version.setTimestamp(0L);
            version.setStateVersion(0L);
            return body;
        }

        try {
            body = RegisterBrokerBody.decode(request.getBody(), requestHeader.isCompressed());
        } catch (Exception e) {
            throw new RemotingCommandException("Failed to decode RegisterBrokerBody", e);
        }

        return body;
    }

    private StoreInfo getStoreInfo(RpcCommand request, RegisterBrokerRequestHeader requestHeader, RegisterBrokerBody body) {
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setClusterName(requestHeader.getClusterName());
        storeInfo.setGroupName(requestHeader.getBrokerName());
        storeInfo.setGroupNo(requestHeader.getBrokerId());
        storeInfo.setAddress(requestHeader.getBrokerAddr());
        storeInfo.setZoneName(request.getExtFields().get(MQConstants.ZONE_MODE));
        storeInfo.setHaAddress(requestHeader.getHaServerAddr());

        Integer timeout = null != requestHeader.getHeartbeatTimeoutMillis()
            ? requestHeader.getHeartbeatTimeoutMillis().intValue()
            : null;
        storeInfo.setHeartbeatTimeout(timeout);
        storeInfo.setEnableMasterElection(requestHeader.getEnableActingMaster());

        storeInfo.setTopicInfo(body.getTopicConfigSerializeWrapper());
        storeInfo.setFilterList(body.getFilterServerList());

        return storeInfo;
    }

    private RegisterStoreResult registerStore(RpcContext ctx, RpcCommand request, RegisterBrokerRequestHeader requestHeader) throws RemotingCommandException {
        RegisterBrokerBody body = extractBody(request, requestHeader);
        StoreInfo storeInfo = getStoreInfo(request, requestHeader, body);

        return storeRegistry.register(storeInfo, ctx.channel());
    }

    private void formatResponseHeader(RegisterStoreResult result, RegisterBrokerResponseHeader responseHeader) {
        responseHeader.setHaServerAddr(result.getHaServerAddr());
        responseHeader.setMasterAddr(result.getMasterAddr());
    }

    private void addOrderConfig(RpcCommand response) {
        if (registryConfig.isReturnOrderTopic()) {
            byte[] orderConfig = kvService.getKVListByNamespace(KVService.NAMESPACE_ORDER_TOPIC_CONFIG);
            response.setBody(orderConfig);
        }
    }

}
