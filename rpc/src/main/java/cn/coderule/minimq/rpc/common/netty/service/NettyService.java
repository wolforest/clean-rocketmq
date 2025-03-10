package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.minimq.rpc.common.core.RpcService;
import cn.coderule.minimq.rpc.common.RpcHook;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class NettyService implements RpcService {
    private static final int DEFAULT_PROCESSOR_THREAD_NUM = 4;

    protected final NettyDispatcher dispatcher;
    protected final NettyInvoker invoker;
    protected final ExecutorService callbackExecutor;

    protected AtomicBoolean stopping = new AtomicBoolean(false);

    public NettyService(int onewaySemaphorePermits, int asyncSemaphorePermits, int callbackThreadNum) {
        this.dispatcher = new NettyDispatcher();
        this.callbackExecutor = buildCallbackExecutor(callbackThreadNum);
        this.invoker = new NettyInvoker(onewaySemaphorePermits, asyncSemaphorePermits, callbackExecutor);
    }

    @Override
    public void registerRpcHook(RpcHook rpcHook) {
        dispatcher.registerRpcHook(rpcHook);
    }

    @Override
    public void clearRpcHook() {
        dispatcher.clearRpcHook();
    }

    @Override
    public void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor) {
        ExecutorService executorService = executor == null ? this.getCallbackExecutor() : executor;
        dispatcher.registerProcessor(requestCode, processor, executorService);
    }

    @Override
    public void registerProcessor(Collection<Integer> codes, RpcProcessor processor, ExecutorService executor) {
        ExecutorService executorService = executor == null ? this.getCallbackExecutor() : executor;
        dispatcher.registerProcessor(codes, processor, executorService);
    }

    public void failFast(Channel channel) {
        this.invoker.failFast(channel);
    }

    private ExecutorService buildCallbackExecutor(int callbackThreadNum) {
        int threadNum = callbackThreadNum;
        if (threadNum <= 0) {
            threadNum = DEFAULT_PROCESSOR_THREAD_NUM;
        }

        return Executors.newFixedThreadPool(threadNum, new DefaultThreadFactory("NettyProcessor_"));
    }

}
