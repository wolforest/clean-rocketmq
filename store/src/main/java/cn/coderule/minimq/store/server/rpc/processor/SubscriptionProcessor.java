package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.minimq.domain.service.store.api.SubscriptionStore;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;

public class SubscriptionProcessor implements RpcProcessor {
    private final SubscriptionStore subscriptionStore;
    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG,
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
            case RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG -> this.getGroup(ctx, request);
            case RequestCode.DELETE_SUBSCRIPTIONGROUP -> this.deleteGroup(ctx, request);
            case RequestCode.UPDATE_AND_CREATE_SUBSCRIPTIONGROUP -> this.saveGroup(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand saveGroup(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }

    private RpcCommand getGroup(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }

    private RpcCommand deleteGroup(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }
}
