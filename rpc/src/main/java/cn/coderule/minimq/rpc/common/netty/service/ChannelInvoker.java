package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.lang.concurrent.SemaphoreGuard;
import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTooMuchRequestException;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelInvoker {
    private final HashedWheelTimer timer;
    private final Semaphore onewaySemaphore;
    private final Semaphore asyncSemaphore;
    private final ExecutorService callbackExecutor;

    /**
     * response map
     * { opaque : ResponseFuture }
     */
    private final ConcurrentMap<Integer, ResponseFuture> responseMap
        = new ConcurrentHashMap<>(256);

    public ChannelInvoker(int onewayPermits, int asyncPermits, ExecutorService callbackExecutor) {
        this.onewaySemaphore = new Semaphore(onewayPermits);
        this.asyncSemaphore = new Semaphore(asyncPermits);
        this.callbackExecutor = callbackExecutor;
        this.timer = new HashedWheelTimer(r -> new Thread(r, "NettyTimer"));
    }

    public void start() {
        TimerTask task = new TimerTask() {
            @Override
            public void run (Timeout timeout) {
                try {
                    ChannelInvoker.this.scanResponse();
                } catch (Throwable t) {
                    log.error("NettyInvoker.scanResponse exception", t);
                } finally {
                    timer.newTimeout(this, 1000, TimeUnit.MILLISECONDS);
                }
            }
        };
        timer.newTimeout(task, 1000, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        timer.stop();
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
        long startTime = System.currentTimeMillis();
        CompletableFuture<ResponseFuture> future = new CompletableFuture<>();
        SemaphoreGuard semaphoreGuard = new SemaphoreGuard(this.asyncSemaphore);

        if (!tryAcquireAsyncSemaphore(timeoutMillis, future, semaphoreGuard, startTime)) {
            return future;
        }

        ResponseFuture response = createResponseFuture(channel, request, timeoutMillis, future, semaphoreGuard);
        this.responseMap.put(request.getOpaque(), response);

        try {
            return writeAndFlush(channel, request, response, future);
        } catch (Exception e) {
            return flushFailed(channel, request, response, future, e);
        }
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

    public void failFast(final Channel channel) {
        for (Map.Entry<Integer, ResponseFuture> entry : responseMap.entrySet()) {
            if (entry.getValue().getChannel() != channel) {
                continue;
            }

            Integer opaque = entry.getKey();
            if (opaque != null) {
                requestFailed(opaque);
            }
        }
    }

    public void scanResponse() {
        List<ResponseFuture> rfList = new LinkedList<>();
        Iterator<Map.Entry<Integer, ResponseFuture>> it = this.responseMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            long maxTime = rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000;
            if (maxTime > System.currentTimeMillis()) {
                continue;
            }

            rep.release();
            it.remove();
            rfList.add(rep);
            log.warn("remove timeout request, {}", rep);
        }

        executeInvokeCallback(rfList);
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

    private boolean tryAcquireAsyncSemaphore(long timeoutMillis, CompletableFuture<ResponseFuture> future, SemaphoreGuard semaphoreGuard, long startTime) {
        boolean acquired;
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

    private ResponseFuture createResponseFuture(Channel channel, RpcCommand request, long timeout, CompletableFuture<ResponseFuture> future, SemaphoreGuard semaphoreGuard) {
        AtomicReference<ResponseFuture> responseReference = new AtomicReference<>();
        RpcCallback rpcCallback = new RpcCallback() {
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

    private boolean submitInvokeCallback(final ResponseFuture responseFuture, ExecutorService executor) {
        boolean runInThisThread = false;

        try {
            executor.submit(() -> {
                callInvokeCallback(responseFuture);
            });
        } catch (Exception e) {
            runInThisThread = true;
            log.warn("execute callback in executor exception, maybe executor busy", e);
        }

        return runInThisThread;
    }

    private void callInvokeCallback(final ResponseFuture responseFuture) {
        try {
            responseFuture.executeRpcCallback();
        } catch (Throwable e) {
            log.warn("executeInvokeCallback Exception", e);
        } finally {
            responseFuture.release();
        }
    }

    private void executeInvokeCallback(List<ResponseFuture> rfList) {
        for (ResponseFuture rf : rfList) {
            try {
                executeInvokeCallback(rf);
            } catch (Throwable e) {
                log.warn("scanResponseTable, operationComplete Exception", e);
            }
        }
    }

    private void executeInvokeCallback(final ResponseFuture responseFuture) {
        ExecutorService executor = this.callbackExecutor;
        boolean runInThisThread = executor == null || executor.isShutdown();

        if (!runInThisThread) {
            runInThisThread = submitInvokeCallback(responseFuture, executor);
        }

        if (runInThisThread) {
            callInvokeCallback(responseFuture);
        }
    }

    private void requestFailed(final int opaque) {
        ResponseFuture responseFuture = responseMap.remove(opaque);
        if (responseFuture == null) {
            return;
        }

        responseFuture.setSendRequestOK(false);
        responseFuture.putResponse(null);
        try {
            executeInvokeCallback(responseFuture);
        } catch (Throwable e) {
            log.warn("execute callback in requestFail, and callback throw", e);
        } finally {
            responseFuture.release();
        }
    }

    private CompletableFuture<ResponseFuture> writeAndFlush(Channel channel, RpcCommand request, ResponseFuture response, CompletableFuture<ResponseFuture> future) {
        channel.writeAndFlush(request)
            .addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    response.setSendRequestOK(true);
                    return;
                }

                requestFailed(request.getOpaque());
                log.warn("send a request command to channel <{}> failed.", NettyHelper.getRemoteAddr(channel));
            });
        return future;
    }

    private CompletableFuture<ResponseFuture> flushFailed(Channel channel, RpcCommand request, ResponseFuture response, CompletableFuture<ResponseFuture> future, Exception e) {
        responseMap.remove(request.getOpaque());
        response.release();
        log.warn("send a request command to channel <{}> Exception", NettyHelper.getRemoteAddr(channel), e);
        future.completeExceptionally(new RemotingSendRequestException(NettyHelper.getRemoteAddr(channel), e));
        return future;
    }

}
