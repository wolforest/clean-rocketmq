package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.minimq.domain.service.store.api.SubscriptionStore;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyHelper;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import cn.coderule.minimq.rpc.store.protocol.header.DeleteSubscriptionGroupRequestHeader;
import cn.coderule.minimq.rpc.store.protocol.header.GetSubscriptionGroupConfigRequestHeader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionProcessor implements RpcProcessor {
    private final SubscriptionStore subscriptionStore;
    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_SUBSCRIPTIONGROUP_CONFIG,
        RequestCode.DELETE_SUBSCRIPTIONGROUP,
        RequestCode.UPDATE_AND_CREATE_SUBSCRIPTIONGROUP
    );

    public SubscriptionProcessor(SubscriptionStore subscriptionStore, ExecutorService executor) {
        this.subscriptionStore = subscriptionStore;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.UPDATE_AND_CREATE_SUBSCRIPTIONGROUP -> this.saveGroup(ctx, request);
            case RequestCode.DELETE_SUBSCRIPTIONGROUP -> this.deleteGroup(ctx, request);
            case RequestCode.GET_SUBSCRIPTIONGROUP_CONFIG -> this.getGroup(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand saveGroup(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        log.info("receive request to save group, caller address={}", NettyHelper.getRemoteAddr(ctx.channel()));
        RpcCommand response = RpcCommand.createResponseCommand(null);

        SubscriptionGroup group = RpcSerializable.decode(request.getBody(), SubscriptionGroup.class);
        if (group == null) {
            return response.success();
        }

        try {
            subscriptionStore.saveGroup(group);
        } catch (Exception e) {
            log.error("save group error", e);
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "save group error");
        }

        return response.success();
    }

    private RpcCommand deleteGroup(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        DeleteSubscriptionGroupRequestHeader requestHeader = request.decodeHeader(DeleteSubscriptionGroupRequestHeader.class);
        log.info("receive request to delete group={}, caller address={}",
            requestHeader.getGroupName(), NettyHelper.getRemoteAddr(ctx.channel()));

        try {
            subscriptionStore.deleteGroup(requestHeader.getGroupName(), requestHeader.isCleanOffset());
        } catch (Exception e) {
            log.error("delete group error", e);
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "delete group error");
        }

        return response.success();
    }

    private RpcCommand getGroup(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        GetSubscriptionGroupConfigRequestHeader requestHeader = request.decodeHeader(GetSubscriptionGroupConfigRequestHeader.class);

        try {
            SubscriptionGroup group = subscriptionStore.getGroup(requestHeader.getGroup());
            if (group == null) {
                return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "group not exist");
            }

            response.setBody(RpcSerializable.encode(group));
        } catch (Exception e) {
            log.error("get group error", e);
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "get group error");
        }

        return response.success();
    }


}
