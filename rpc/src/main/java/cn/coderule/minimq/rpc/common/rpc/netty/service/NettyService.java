package cn.coderule.minimq.rpc.common.rpc.netty.service;

import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.rpc.RpcHook;
import cn.coderule.minimq.rpc.common.rpc.core.RpcService;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyDispatcher;
import cn.coderule.minimq.rpc.common.rpc.netty.service.invoker.ChannelInvoker;
import io.netty.channel.Channel;
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
    protected final ChannelInvoker invoker;
    protected final ExecutorService callbackExecutor;

    protected AtomicBoolean stopping = new AtomicBoolean(false);

    public NettyService(int onewaySemaphorePermits, int asyncSemaphorePermits, int callbackThreadNum) {
        this.callbackExecutor = buildCallbackExecutor(callbackThreadNum);
        this.dispatcher = new NettyDispatcher(this.callbackExecutor);
        this.invoker = new ChannelInvoker(onewaySemaphorePermits, asyncSemaphorePermits, dispatcher);
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
    public void registerDefaultProcessor(RpcProcessor processor, ExecutorService executor) {
        dispatcher.registerDefaultProcessor(processor, executor);
    }

    @Override
    public void registerProcessor(RpcProcessor processor) {
        if (CollectionUtil.isEmpty(processor.getCodeSet())) {
            return;
        }

        dispatcher.registerProcessor(processor.getCodeSet(), processor, processor.getExecutor());
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
        this.dispatcher.failFast(channel);
    }

    private ExecutorService buildCallbackExecutor(int callbackThreadNum) {
        int threadNum = callbackThreadNum;
        if (threadNum <= 0) {
            threadNum = DEFAULT_PROCESSOR_THREAD_NUM;
        }

        return Executors.newFixedThreadPool(threadNum, new DefaultThreadFactory("NettyProcessor_"));
    }

}
