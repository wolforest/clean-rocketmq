package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.RpcService;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.RpcHook;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.netty.event.NettyEvent;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import io.netty.handler.ssl.SslContext;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class NettyService implements RpcService {
    protected final Semaphore onewaySemaphore;
    protected final Semaphore asyncSemaphore;

    protected final NettyDispatcher dispatcher;

    /**
     * response map
     * { opaque : ResponseFuture }
     */
    protected final ConcurrentMap<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);

    protected final NettyEventExecutor nettyEventExecutor = new NettyEventExecutor(this);

    protected volatile SslContext sslContext;
    protected AtomicBoolean stopping = new AtomicBoolean(false);

    public NettyService(int onewaySemaphorePermits, int asyncSemaphorePermits) {
        this.onewaySemaphore = new Semaphore(onewaySemaphorePermits, true);
        this.asyncSemaphore = new Semaphore(asyncSemaphorePermits, true);
        this.dispatcher = new NettyDispatcher();
    }

    /**
     * Put a netty event to the executor.
     *
     * @param event Netty event instance.
     */
    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecutor.putNettyEvent(event);
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

}
