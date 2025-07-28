package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.minimq.domain.service.store.api.TimerStore;
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
public class TimerProcessor implements RpcProcessor {
    private final TimerStore timerStore;
    @Getter
    private final ExecutorService executor;

    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_TIMER_CHECK_POINT,
        RequestCode.GET_TIMER_METRICS
    );

    public TimerProcessor(TimerStore timerStore, ExecutorService executor) {
        this.timerStore = timerStore;
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
