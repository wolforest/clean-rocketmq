package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.registry.domain.store.service.ClusterService;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.header.AddWritePermOfBrokerResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.BrokerHeartbeatRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.WipeWritePermOfBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.WipeWritePermOfBrokerResponseHeader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterProcessor implements RpcProcessor {
    private final ClusterService clusterService;

    public ClusterProcessor(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.GET_BROKER_CLUSTER_INFO -> this.getClusterInfo(ctx, request);
            case RequestCode.GET_BROKER_MEMBER_GROUP -> this.getGroupInfo(ctx, request);

            case RequestCode.QUERY_DATA_VERSION -> this.getStoreVersion(ctx, request);
            case RequestCode.BROKER_HEARTBEAT -> this.flushStoreUpdateTime(ctx, request);

            case RequestCode.WIPE_WRITE_PERM_OF_BROKER -> this.removeGroupWritePermission(ctx, request);
            case RequestCode.ADD_WRITE_PERM_OF_BROKER -> this.addGroupWritePermission(ctx, request);
            default -> {
                String error = " request type " + request.getCode() + " not supported";
                yield RpcCommand.createResponseCommand(SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
            }
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand getStoreVersion(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    private RpcCommand getClusterInfo(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        byte[] content = clusterService.getClusterInfo().encode();
        response.setBody(content);

        return response.success();

    }

    private RpcCommand getGroupInfo(RpcContext ctx, RpcCommand request) throws RemotingCommandException {

        return null;
    }

    private RpcCommand removeGroupWritePermission(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(WipeWritePermOfBrokerResponseHeader.class);
        WipeWritePermOfBrokerResponseHeader responseHeader = (WipeWritePermOfBrokerResponseHeader) response.readCustomHeader();
        WipeWritePermOfBrokerRequestHeader requestHeader = request.decodeHeader(WipeWritePermOfBrokerRequestHeader.class);

        int result = clusterService.removeGroupWritePermission(requestHeader.getBrokerName());

        if (ctx != null) {
            log.info("wipe write perm of group[{}], client: {}, {}",
                requestHeader.getBrokerName(),
                NettyHelper.getRemoteAddr(ctx.channel()),
                result
            );
        }

        responseHeader.setWipeTopicCount(result);
        return response.success();
    }

    private RpcCommand addGroupWritePermission(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(AddWritePermOfBrokerResponseHeader.class);
        AddWritePermOfBrokerResponseHeader responseHeader = (AddWritePermOfBrokerResponseHeader) response.readCustomHeader();
        WipeWritePermOfBrokerRequestHeader requestHeader = request.decodeHeader(WipeWritePermOfBrokerRequestHeader.class);

        int result = clusterService.addGroupWritePermission(requestHeader.getBrokerName());

        log.info("add write perm of group[{}], client: {}, {}",
            requestHeader.getBrokerName(),
            NettyHelper.getRemoteAddr(ctx.channel()),
            result
        );

        responseHeader.setAddTopicCount(result);
        return response.success();
    }

    private RpcCommand flushStoreUpdateTime(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        BrokerHeartbeatRequestHeader requestHeader = request.decodeHeader(BrokerHeartbeatRequestHeader.class);

        clusterService.flushStoreUpdateTime(requestHeader.getClusterName(), requestHeader.getBrokerAddr());

        return response.success();
    }

}
