package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.ds.Pair;
import cn.coderule.minimq.rpc.common.core.RpcService;
import cn.coderule.minimq.rpc.common.core.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;
import cn.coderule.minimq.rpc.common.core.RpcProcessor;
import cn.coderule.minimq.rpc.common.netty.event.NettyEvent;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import io.netty.handler.ssl.SslContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * response map
     * { opaque : ResponseFuture }
     */
    protected final ConcurrentMap<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);
    /**
     * processor map
     * { requestCode: [RpcProcessor, ExecutorService] }
     */
    protected final HashMap<Integer, Pair<RpcProcessor, ExecutorService>> processorMap = new HashMap<>(64);
    protected final NettyEventExecutor nettyEventExecutor = new NettyEventExecutor(this);

    protected volatile SslContext sslContext;
    protected List<RpcHook> rpcHooks = new ArrayList<>();
    protected RpcPipeline rpcPipeline;
    protected AtomicBoolean isStopping = new AtomicBoolean(false);
    protected Pair<RpcProcessor, ExecutorService> defaultProcessorPair;

    public NettyService(int onewaySemaphorePermits, int asyncSemaphorePermits) {
        this.onewaySemaphore = new Semaphore(onewaySemaphorePermits, true);
        this.asyncSemaphore = new Semaphore(asyncSemaphorePermits, true);
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
        if (rpcHook != null && !rpcHooks.contains(rpcHook)) {
            rpcHooks.add(rpcHook);
        }
    }

    @Override
    public void clearRpcHook() {
        rpcHooks.clear();
    }

    @Override
    public void setRpcPipeline(RpcPipeline pipeline) {
        this.rpcPipeline = pipeline;
    }

}
