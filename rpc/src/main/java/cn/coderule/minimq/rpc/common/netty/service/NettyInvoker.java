package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTooMuchRequestException;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    private boolean tryAcquireAsyncSemaphore(long timeoutMillis, CompletableFuture<ResponseFuture> future) {
        boolean acquired;
        try {
            acquired = this.asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            future.completeExceptionally(t);
            return false;
        }

        if (acquired) return true;

        if (timeoutMillis <= 0) {
            future.completeExceptionally(new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast"));
            return false;
        }

        String info = String.format(
            "invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
            timeoutMillis,
            this.asyncSemaphore.getQueueLength(),
            this.asyncSemaphore.availablePermits()
        );
        log.warn(info);
        future.completeExceptionally(new RemotingTimeoutException(info));
        return false;
    }

    public CompletableFuture<ResponseFuture> invokeAsync(Channel channel, RpcCommand request, long timeoutMillis) {
        CompletableFuture<ResponseFuture> future = new CompletableFuture<>();

        if (!tryAcquireAsyncSemaphore(timeoutMillis, future)) {
            return future;
        }

        return null;
    }

    public void invokeAsync(Channel channel, RpcCommand request, long timeoutMillis, RpcCallback rpcCallback) {
        invokeAsync(channel, request, timeoutMillis)
        .whenComplete((v, t) -> {
            if (t == null) {
                rpcCallback.onComplete(v);
            } else {
                ResponseFuture responseFuture = new ResponseFuture(
                    channel,
                    request.getOpaque(),
                    request,
                    timeoutMillis,
                    null,
                    null
                );

                responseFuture.setCause(t);
                rpcCallback.onComplete(responseFuture);
            }
        }).thenAccept(responseFuture -> {
            rpcCallback.onSuccess(responseFuture.getResponse());
        }).exceptionally(t -> {
            rpcCallback.onFailure(t);
            return null;
        });
    }

    public void scanResponseMap() {

    }
}
