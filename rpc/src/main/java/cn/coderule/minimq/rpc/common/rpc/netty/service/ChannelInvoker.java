package cn.coderule.minimq.rpc.common.rpc.netty.service;

import cn.coderule.common.lang.concurrent.sync.SemaphoreGuard;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingTooMuchRequestException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.ResponseFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelInvoker {
    private final NettyDispatcher dispatcher;
    private final Semaphore onewaySemaphore;
    private final Semaphore asyncSemaphore;

    public ChannelInvoker(int onewayPermits, int asyncPermits, NettyDispatcher dispatcher) {
        this.onewaySemaphore = new Semaphore(onewayPermits);
        this.asyncSemaphore = new Semaphore(asyncPermits);
        this.dispatcher = dispatcher;
    }

    public void invokeOneway(Channel channel, RpcCommand request, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingTooMuchRequestException, RemotingSendRequestException {
        request.markOnewayRPC();

        boolean acquired = this.onewaySemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (!acquired) {
            acquireOnewaySemaphoreFailed(timeoutMillis);
            return;
        }

        SemaphoreGuard guard = new SemaphoreGuard(this.onewaySemaphore);
        try {
            channel.writeAndFlush(request)
            .addListener((ChannelFutureListener) f -> {
                guard.release();
                if (!f.isSuccess()) {
                    log.warn("invokeOneway failed: {}", channel.remoteAddress());
                }
            });
        } catch (Exception e) {
            guard.release();
            log.warn("invokeOneway flush failed: {}", channel.remoteAddress());
            throw new RemotingSendRequestException(NettyHelper.getRemoteAddr(channel), e);
        }
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
        CompletableFuture<ResponseFuture> future = new CompletableFuture<>();
        SemaphoreGuard semaphoreGuard = new SemaphoreGuard(this.asyncSemaphore);
        if (!tryAcquireAsyncSemaphore(timeoutMillis, future, semaphoreGuard)) {
            return future;
        }

        ResponseFuture response = createResponseFuture(channel, request, timeoutMillis, future, semaphoreGuard);
        this.dispatcher.putResponse(request.getOpaque(), response);

        try {
            writeAndFlush(channel, request, response);
            return future;
        } catch (Exception e) {
            return flushFailed(channel, request, response, future, e);
        }
    }

    public void invokeAsync(Channel channel, RpcCommand request, long timeout, RpcCallback rpcCallback) {
        invokeAsync(channel, request, timeout)
        .whenComplete((v, t) -> {
            if (t == null) {
                rpcCallback.onComplete(v);
                return;
            }

            ResponseFuture responseFuture = new ResponseFuture(channel, request, timeout);
            responseFuture.setCause(t);
            rpcCallback.onComplete(responseFuture);
        }).thenAccept(responseFuture -> {
            rpcCallback.onSuccess(responseFuture.getResponse());
        }).exceptionally(t -> {
            rpcCallback.onFailure(t);
            return null;
        });
    }

    /*********************************** private methods ***********************************/
    private void acquireOnewaySemaphoreFailed(long timeoutMillis) throws RemotingTooMuchRequestException, RemotingTimeoutException {
        if (timeoutMillis <= 0) {
            throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
        }

        String info = String.format(
            "invokeOneway tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreOnewayValue: %d",
            timeoutMillis,
            this.onewaySemaphore.getQueueLength(),
            this.onewaySemaphore.availablePermits()
        );
        log.warn(info);
        throw new RemotingTimeoutException(info);
    }

    private void acquireAsyncSemaphoreFailed(long timeoutMillis, CompletableFuture<ResponseFuture> future) {
        if (timeoutMillis <= 0) {
            future.completeExceptionally(new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast"));
            return;
        }

        String info = String.format(
            "invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
            timeoutMillis,
            this.asyncSemaphore.getQueueLength(),
            this.asyncSemaphore.availablePermits()
        );
        log.warn(info);
        future.completeExceptionally(new RemotingTimeoutException(info));
    }

    private boolean tryAcquireAsyncSemaphore(long timeoutMillis, CompletableFuture<ResponseFuture> future, SemaphoreGuard semaphoreGuard) {
        boolean acquired;
        long startTime = System.currentTimeMillis();
        try {
            acquired = this.asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            future.completeExceptionally(t);
            return false;
        }

        if (!acquired) {
            acquireAsyncSemaphoreFailed(timeoutMillis, future);
            return false;
        }

        long costTime = System.currentTimeMillis() - startTime;
        if (timeoutMillis < costTime) {
            semaphoreGuard.release();
            future.completeExceptionally(new RemotingTimeoutException("invokeAsyncImpl call timeout"));
            return false;
        }

        return true;
    }

    private RpcCallback createRpcCallback(CompletableFuture<ResponseFuture> future, AtomicReference<ResponseFuture> responseReference) {
        return new RpcCallback() {
            @Override
            public void onComplete(ResponseFuture responseFuture) {
            }

            @Override
            public void onSuccess(RpcCommand response) {
                future.complete(responseReference.get());
            }

            @Override
            public void onFailure(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        };
    }

    private ResponseFuture createResponseFuture(Channel channel, RpcCommand request, long timeout, CompletableFuture<ResponseFuture> future, SemaphoreGuard semaphoreGuard) {
        AtomicReference<ResponseFuture> responseReference = new AtomicReference<>();
        RpcCallback rpcCallback = createRpcCallback(future, responseReference);

        ResponseFuture responseFuture = new ResponseFuture(
            channel,
            request.getOpaque(),
            request,
            timeout,
            rpcCallback,
            semaphoreGuard
        );
        responseReference.set(responseFuture);

        return responseFuture;
    }

    private void writeAndFlush(Channel channel, RpcCommand request, ResponseFuture response) {
        channel.writeAndFlush(request)
            .addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    response.setSendRequestOK(true);
                    return;
                }

                dispatcher.failFast(request.getOpaque());
                log.warn("send a request command to channel <{}> failed.", NettyHelper.getRemoteAddr(channel));
            });
    }

    private CompletableFuture<ResponseFuture> flushFailed(Channel channel, RpcCommand request, ResponseFuture response, CompletableFuture<ResponseFuture> future, Exception e) {
        dispatcher.removeResponse(request.getOpaque());
        response.release();

        log.warn("send a request command to channel [{}] Exception", NettyHelper.getRemoteAddr(channel), e);
        future.completeExceptionally(new RemotingSendRequestException(NettyHelper.getRemoteAddr(channel), e));
        return future;
    }

}
