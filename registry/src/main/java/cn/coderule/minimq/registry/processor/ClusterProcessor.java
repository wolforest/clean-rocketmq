package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.registry.domain.store.service.ClusterService;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.netty.service.NettyHelper;
import cn.coderule.minimq.domain.domain.model.DataVersion;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.rpc.registry.protocol.body.BrokerMemberGroup;
import cn.coderule.minimq.rpc.registry.protocol.body.GetBrokerMemberGroupResponseBody;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.AddWritePermOfBrokerResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.BrokerHeartbeatRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.GetBrokerMemberGroupRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.QueryDataVersionRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.QueryDataVersionResponseHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.WipeWritePermOfBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.WipeWritePermOfBrokerResponseHeader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterProcessor implements RpcProcessor {
    private final ClusterService clusterService;

    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_BROKER_CLUSTER_INFO,
        RequestCode.GET_BROKER_MEMBER_GROUP,
        RequestCode.QUERY_DATA_VERSION,
        RequestCode.BROKER_HEARTBEAT,
        RequestCode.WIPE_WRITE_PERM_OF_BROKER,
        RequestCode.ADD_WRITE_PERM_OF_BROKER
    );

    public ClusterProcessor(ClusterService clusterService, ExecutorService executor) {
        this.clusterService = clusterService;
        this.executor = executor;
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
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand getStoreVersion(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(QueryDataVersionResponseHeader.class);
        QueryDataVersionResponseHeader responseHeader = (QueryDataVersionResponseHeader) response.readCustomHeader();
        QueryDataVersionRequestHeader requestHeader = request.decodeHeader(QueryDataVersionRequestHeader.class);

        DataVersion requestVersion = RpcSerializable.decode(request.getBody(), DataVersion.class);
        StoreInfo store = new StoreInfo(requestHeader.getClusterName(), requestHeader.getBrokerAddr());

        clusterService.flushStoreUpdateTime(requestHeader.getClusterName(), requestHeader.getBrokerAddr());

        DataVersion version = clusterService.getStoreVersion(store);
        if (version != null) {
            response.setBody(RpcSerializable.encode(version));
        }

        boolean changed = version == null || !version.equals(requestVersion);
        responseHeader.setChanged(changed);

        return response.success();
    }

    private RpcCommand getClusterInfo(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        byte[] content = clusterService.getClusterInfo().encode();
        response.setBody(content);

        return response.success();

    }

    private RpcCommand getGroupInfo(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        GetBrokerMemberGroupRequestHeader requestHeader = request.decodeHeader(GetBrokerMemberGroupRequestHeader.class);

        BrokerMemberGroup groupInfo = clusterService.getGroupInfo(requestHeader.getClusterName(), requestHeader.getBrokerName());

        GetBrokerMemberGroupResponseBody body = new GetBrokerMemberGroupResponseBody();
        body.setBrokerMemberGroup(groupInfo);
        response.setBody(body.encode());

        return response.success();
    }

    private RpcCommand removeGroupWritePermission(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(WipeWritePermOfBrokerResponseHeader.class);
        WipeWritePermOfBrokerResponseHeader responseHeader = (WipeWritePermOfBrokerResponseHeader) response.readCustomHeader();
        WipeWritePermOfBrokerRequestHeader requestHeader = request.decodeHeader(WipeWritePermOfBrokerRequestHeader.class);

        int result = clusterService.removeGroupWritePermission(requestHeader.getBrokerName());

        log.info("wipe write perm of group[{}], client: {}, {}",
            requestHeader.getBrokerName(),
            NettyHelper.getRemoteAddr(ctx.channel()),
            result
        );

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
