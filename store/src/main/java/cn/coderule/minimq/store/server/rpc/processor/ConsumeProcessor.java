package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.minimq.domain.service.store.api.meta.ConsumeOffsetStore;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeProcessor implements RpcProcessor {
    private final ConsumeOffsetStore offsetStore;
    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.QUERY_CONSUMER_OFFSET,
        RequestCode.UPDATE_CONSUMER_OFFSET,
        RequestCode.GET_ALL_CONSUMER_OFFSET
    );

    public ConsumeProcessor(ConsumeOffsetStore offsetStore, ExecutorService executor) {
        this.offsetStore = offsetStore;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }
}
