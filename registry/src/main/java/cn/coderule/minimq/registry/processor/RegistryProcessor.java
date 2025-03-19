package cn.coderule.minimq.registry.processor;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.constant.MQConstants;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.store.StoreRegistry;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterBrokerBody;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryProcessor implements RpcProcessor {
    private RegistryConfig registryConfig;
    private StoreRegistry storeRegistry;
    private KVService kvService;

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {

        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }

    public RpcCommand registerStore(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RegisterBrokerRequestHeader requestHeader = request.decodeHeader(RegisterBrokerRequestHeader.class);
        RpcCommand response = RpcCommand.createResponseCommand(RegisterBrokerResponseHeader.class);
        RegisterBrokerResponseHeader responseHeader = (RegisterBrokerResponseHeader) response.readCustomHeader();

        if (!checksum(ctx.getChannelContext(), request, requestHeader, response)) {
            return response;
        }

        StoreRegisterResult result = registerStore(ctx, request, requestHeader);
        if (result == null) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "register failed");
        }

        formatResponseHeader(result, responseHeader);
        addOrderConfig(response);

        return response.setCodeAndRemark(SystemResponseCode.SUCCESS, null);
    }

    public RpcCommand unregisterStore(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        UnRegisterBrokerRequestHeader requestHeader = request.decodeHeader(UnRegisterBrokerRequestHeader.class);

        boolean isSuccess = storeRegistry.unregisterAsync(requestHeader);
        if (!isSuccess) {
            log.warn("unregister store failed, request: {}", requestHeader);
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "unregister failed");
        }
        return response.setCodeAndRemark(SystemResponseCode.SUCCESS, null);
    }

    public RpcCommand registerTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
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

    private StoreInfo getStoreInfo(RpcCommand request, RegisterBrokerRequestHeader requestHeader) {
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setClusterName(requestHeader.getClusterName());
        storeInfo.setGroupName(requestHeader.getBrokerName());
        storeInfo.setGroupNo(requestHeader.getBrokerId());
        storeInfo.setAddress(requestHeader.getBrokerAddr());
        storeInfo.setZoneName(request.getExtFields().get(MQConstants.ZONE_MODE));
        storeInfo.setHaAddress(requestHeader.getHaServerAddr());
        storeInfo.setHeartbeatTimeout(requestHeader.getHeartbeatTimeoutMillis());
        storeInfo.setEnableActingMaster(requestHeader.getEnableActingMaster());

        return storeInfo;
    }

    private StoreRegisterResult registerStore(RpcContext ctx, RpcCommand request, RegisterBrokerRequestHeader requestHeader) throws RemotingCommandException {
        RegisterBrokerBody body = extractBody(request, requestHeader);
        StoreInfo storeInfo = getStoreInfo(request, requestHeader);

        return storeRegistry.register(
            storeInfo,
            body.getTopicConfigSerializeWrapper(),
            body.getFilterServerList(),
            ctx.channel()
        );
    }

    private void formatResponseHeader(StoreRegisterResult result, RegisterBrokerResponseHeader responseHeader) {
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
