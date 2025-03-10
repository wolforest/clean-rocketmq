package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import io.netty.channel.Channel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NettyInvoker {
    private final Semaphore onewaySemaphore;
    private final Semaphore asyncSemaphore;
    private AtomicBoolean stopping = new AtomicBoolean(false);

    /**
     * response map
     * { opaque : ResponseFuture }
     */
    private final ConcurrentMap<Integer, ResponseFuture> responseMap
        = new ConcurrentHashMap<>(256);

    public NettyInvoker(int onewaySemaphorePermits, int asyncSemaphorePermits) {
        this.onewaySemaphore = new Semaphore(onewaySemaphorePermits);
        this.asyncSemaphore = new Semaphore(asyncSemaphorePermits);
    }

    public void shutdown() {
        this.stopping.set(true);
    }

    public RpcCommand invokeSync(Channel channel, RpcCommand request, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException {
        try {
            return invokeAsync(channel, request, timeoutMillis)
                .thenApply(ResponseFuture::getResponse)
                .get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new RemotingSendRequestException(channel.remoteAddress().toString(), e.getCause());
        } catch (TimeoutException e) {
            throw new RemotingTimeoutException(channel.remoteAddress().toString(), timeoutMillis, e.getCause());
        }
    }

    public CompletableFuture<ResponseFuture> invokeAsync(Channel channel, RpcCommand request, long timeoutMillis) {
        return null;
    }

    public void scanResponseMap() {

    }
}
