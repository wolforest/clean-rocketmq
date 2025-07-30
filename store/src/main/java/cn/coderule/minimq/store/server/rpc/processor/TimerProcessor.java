package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.service.store.api.TimerStore;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.SpringApplicationEvent;

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
        return switch (request.getCode()) {
            case RequestCode.GET_TIMER_METRICS -> this.getMetrics(ctx, request);
            case RequestCode.GET_TIMER_CHECK_POINT -> this.getCheckpoint(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand getCheckpoint(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        TimerCheckpoint checkpoint = timerStore.getCheckpoint();

        if (checkpoint == null) {
            log.error("timer checkpoint is null, caller={}", ctx.channel().remoteAddress());
            return response.failure(
                ResponseCode.SYSTEM_ERROR,
                "timer checkpoint is null"
            );
        }

        response.setBody(checkpoint.toBytes());
        return response.success();
    }

    private RpcCommand getMetrics(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }
}
