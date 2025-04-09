package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.core.exception.RemotingConnectException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTooMuchRequestException;
import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.ResponseCode;
import com.google.common.base.Stopwatch;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * address invoker
 * - connection management base on address
 * - invoke and retry policy
 */
@Slf4j
public class AddressInvoker {
    private static final long LOCK_TIMEOUT_MILLIS = 3000;
    private static final long MIN_CLOSE_TIMEOUT_MILLIS = 100;

    @Getter
    private final RpcClientConfig config;
    private final Bootstrap bootstrap;
    private final NettyDispatcher dispatcher;
    private final ChannelInvoker channelInvoker;

    private final Lock lock = new ReentrantLock();
    private final ConcurrentMap<String /* addr */, ChannelWrapper> addressMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, ChannelWrapper> channelMap = new ConcurrentHashMap<>();

    public AddressInvoker(RpcClientConfig config, Bootstrap bootstrap, NettyDispatcher dispatcher, ChannelInvoker channelInvoker) {
        this.config = config;
        this.bootstrap = bootstrap;
        this.dispatcher = dispatcher;
        this.channelInvoker = channelInvoker;
    }

    public ChannelFuture getOrCreateChannelAsync(String addr) throws InterruptedException {
        if (StringUtil.isBlank(addr)) {
            return null;
        }

        ChannelWrapper channelWrapper = this.addressMap.get(addr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannelFuture();
        }

        return createChannelAsync(addr);
    }

    public Channel getOrCreateChannel(String addr) throws InterruptedException {
        ChannelFuture channelFuture = getOrCreateChannelAsync(addr);
        if (channelFuture == null) {
            return null;
        }

        return channelFuture.awaitUninterruptibly().channel();
    }

    public RpcCommand invokeSync(String addr, RpcCommand request, long timeout) throws Exception {
        long startTime = System.currentTimeMillis();
        Channel channel = getOrCreateChannel(addr);

        if (channel == null || !channel.isActive()) {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }

        long leftTime = timeout;
        String remoteAddr = NettyHelper.getRemoteAddr(channel);

        try {
            long costTime = System.currentTimeMillis() - startTime;
            leftTime -= costTime;
            if (leftTime <= 0) {
                throw new RemotingTimeoutException(addr, timeout);
            }
            RpcCommand response = this.invokeWithRetry(channel, request, leftTime)
                .thenApply(ResponseFuture::getResponse)
                .get(leftTime, TimeUnit.MILLISECONDS);

            updateLastResponseTime(addr);
            return response;
        } catch (ExecutionException e) {
            log.warn("invokeSync: send request exception, so close the channel[{}]", remoteAddr);
            this.closeChannel(addr, channel);
            throw e;
        } catch (RemotingTimeoutException e) {
            invokeSyncTimeout(addr, channel, leftTime, timeout);
            throw e;
        }
    }


    public void invokeAsync(String addr, RpcCommand request, long timeout, RpcCallback rpcCallback) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ChannelFuture future = getOrCreateChannelAsync(addr);
        if (future == null) {
            rpcCallback.onFailure(new RemotingConnectException(addr));
            return;
        }

        future.addListener(
            createInvokeFutureListener(addr, request, timeout, rpcCallback, startTime, future)
        );
    }

    public CompletableFuture<RpcCommand> invokeAsync(String addr, RpcCommand request, long timeout) {
        CompletableFuture<RpcCommand> future = new CompletableFuture<>();

        try {
            ChannelFuture channelFuture = getOrCreateChannelAsync(addr);
            if (channelFuture == null) {
                future.completeExceptionally(new RemotingConnectException(addr));
                return future;
            }

            addInvokeAsyncListener(addr, request, timeout, channelFuture, future);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

    public void invokeOneway(String addr, RpcCommand request, long timeout) throws Exception {
        ChannelFuture channelFuture = getOrCreateChannelAsync(addr);
        if (channelFuture == null) {
            throw new RemotingConnectException(addr);
        }

        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                return;
            }

            Channel channel = channelFuture.channel();
            String remoteAddr = NettyHelper.getRemoteAddr(channel);
            if (remoteAddr == null || !channel.isActive()) {
                this.closeChannel(addr, channel);
                return;
            }

            RpcContext ctx = new RpcContext(remoteAddr);
            dispatcher.invokePreHooks(ctx, request);

            channelInvoker.invokeOneway(channel, request, timeout);
        });
    }

    public Bootstrap getBootstrap(String addr) {
        return bootstrap;
    }

    public void updateLastResponseTime(String addr) {
        if (StringUtil.isBlank(addr)) {
            return;
        }

        ChannelWrapper channelWrapper = this.addressMap.get(addr);
        if (channelWrapper == null) {
            return;
        }

        if (!channelWrapper.isOK()) {
            return;
        }

        channelWrapper.updateLastResponseTime();
    }

    public boolean isChannelWritable(String addr) {
        if (StringUtil.isBlank(addr)) {
            return false;
        }

        ChannelWrapper cw = this.addressMap.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.isWritable();
        }
        return true;
    }

    public boolean isAddressReachable(String addr) {
        if (StringUtil.isBlank(addr)) {
            return false;
        }

        try {
            Channel channel = getOrCreateChannel(addr);
            return channel != null && channel.isActive();
        } catch (Exception e) {
            log.warn("Get and create channel of {} failed", addr, e);
            return false;
        }
    }

    public void closeChannel(String addr, Channel channel) {
        if (null == channel) {
            return;
        }

        String remoteAddr = null != addr
            ? addr
            : NettyHelper.getRemoteAddr(channel);

        if (StringUtil.isBlank(remoteAddr)) {
            return;
        }

        try {
            if (!lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
                return;
            }

            closeChannelWithLock(remoteAddr, channel);
        } catch (InterruptedException e) {
            log.error("closeChannel exception", e);
        }
    }

    public void closeChannel(Channel channel) {
        if (null == channel) {
            return;
        }

        try {
            if (!lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                log.warn("closeChannel: fail to  lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
                return;
            }

            closeChannelWithLock(channel);
        } catch (InterruptedException e) {
            log.error("closeChannel exception", e);
        }
    }

    public void closeChannels(List<String> addrList) {
        if (CollectionUtil.isEmpty(addrList)) {
            return;
        }

        for (String addr : addrList) {
            ChannelWrapper wrapper = this.addressMap.get(addr);
            if (wrapper == null) {
                continue;
            }
            this.closeChannel(addr, wrapper.getChannel());
        }

        dispatcher.interruptRequests(new HashSet<>(addrList));
    }

    /******************************* private methods start ***********************************/
    private ChannelFuture createChannelAsync(final String addr) throws InterruptedException {
        ChannelWrapper channelWrapper = this.addressMap.get(addr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannelFuture();
        }

        if (!lock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            return null;
        }

        try {
            channelWrapper = this.addressMap.get(addr);
            if (channelWrapper == null) {
                return createChannel(addr).getChannelFuture();
            }

            if (channelWrapper.isOK() || !channelWrapper.getChannelFuture().isDone()) {
                return channelWrapper.getChannelFuture();
            } else {
                this.addressMap.remove(addr);
            }

            return createChannel(addr).getChannelFuture();
        } catch (Exception e) {
            log.error("CreateChannel exception", e);
        } finally {
            lock.unlock();
        }

        return null;
    }

    private ChannelWrapper createChannel(String addr) {
        String[] hostAndPort = NetworkUtil.getHostAndPort(addr);
        ChannelFuture channelFuture = bootstrap.connect(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
        log.info("createChannel: begin to connect remote host[{}] asynchronously", addr);

        ChannelWrapper cw = new ChannelWrapper(this, channelFuture, addr);
        this.addressMap.put(addr, cw);
        this.channelMap.put(channelFuture.channel(), cw);

        return cw;
    }

    private boolean needRemove(ChannelWrapper prevWrapper, Channel channel) {
        boolean shouldRemove = true;
        if (null == prevWrapper) {
            shouldRemove = false;
        } else if (prevWrapper.getChannel() != channel) {
            shouldRemove = false;
        }

        return shouldRemove;
    }

    private void removeChannel(String addr, Channel channel) {
        ChannelWrapper wrapper = this.channelMap.remove(channel);
        if (null != wrapper && wrapper.tryClose(channel)) {
            this.addressMap.remove(addr);
        }
        log.info("closeChannel: the channel[{}] was removed from channel table", addr);
    }

    private void closeChannelWithLock(String addr, Channel channel) {
        try {
            ChannelWrapper prevWrapper = this.addressMap.get(addr);

            boolean shouldRemove = needRemove(prevWrapper, channel);
            if (shouldRemove) {
                removeChannel(addr, channel);
            }

            NettyHelper.close(channel);
        } catch (Exception e) {
            log.error("closeChannel exception", e);
        } finally {
            lock.unlock();
        }
    }

    private void closeChannelWithLock(Channel channel) {
        try {
            ChannelWrapper prevWrapper = null;
            String remoteAddr = null;

            for (Map.Entry<String, ChannelWrapper> entry : this.addressMap.entrySet()) {
                ChannelWrapper value = entry.getValue();
                if (value.getChannel() == null || value.getChannel() != channel) {
                    continue;
                }

                prevWrapper = value;
                remoteAddr = entry.getKey();
                break;
            }

            if (null == prevWrapper) {
                return;
            }

            ChannelWrapper wrapper = this.channelMap.remove(channel);
            if (null != wrapper && wrapper.tryClose(channel)) {
                this.addressMap.remove(remoteAddr);
                log.info("closeChannel: the channel[{}] was removed", remoteAddr);
            }
            NettyHelper.close(channel);
        } catch (Exception e) {
            log.error("closeChannel exception", e);
        } finally {
            lock.unlock();
        }
    }

    private void addInvokeAsyncListener(String addr, RpcCommand request, long timeout, ChannelFuture channelFuture, CompletableFuture<RpcCommand> future) {
        channelFuture.addListener(f -> {
            if (!f.isSuccess()) {
                future.completeExceptionally(new RemotingConnectException(addr));
                return;
            }

            Channel channel = channelFuture.channel();
            if (channel == null || !channel.isActive()) {
                this.closeChannel(addr, channel);
                future.completeExceptionally(new RemotingConnectException(addr));
                return;
            }

            invokeWithRetry(channel, request, timeout)
                .whenComplete((v, t) -> {
                    if (t == null) {
                        updateLastResponseTime(addr);
                    }
                })
                .thenApply(ResponseFuture::getResponse)
                .whenComplete((v, t) -> {
                    if (t == null) {
                        future.complete(v);
                    } else {
                        future.completeExceptionally(t);
                    }
                });
        });
    }

    private void invokeSyncTimeout(String addr, Channel channel, long leftTime, long timeout) throws Exception {
        // avoid close the success channel if left timeout is small,
        // since it may cost too much time in get the success channel, the left timeout for read is small
        boolean shouldClose = leftTime > MIN_CLOSE_TIMEOUT_MILLIS || leftTime > timeout / 4;
        if (shouldClose && config.isCloseChannelWhenTimeout()) {
            this.closeChannel(addr, channel);
            log.warn("invokeSync: close socket because of timeout, {}ms, {}", timeout, addr);
        }

        log.warn("invokeSync: wait response timeout exception, the channel[{}]", addr);
    }

    private CompletableFuture<ResponseFuture> invokeWithRetry(Channel channel, RpcCommand request, long timeout) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        String remoteAddr = NettyHelper.getRemoteAddr(channel);
        RpcContext rpcContext = new RpcContext(remoteAddr);
        dispatcher.invokePreHooks(rpcContext, request);

        return channelInvoker.invokeAsync(channel, request, timeout)
            .thenCompose(responseFuture -> retryInvoke(responseFuture, stopwatch))
            .whenComplete((v, t) -> {
                if (t != null) {
                    return;
                }

                dispatcher.invokePostHooks(rpcContext, request, v.getResponse());
            }) ;
    }

    private CompletableFuture<ResponseFuture> retryInvoke(ResponseFuture responseFuture, Stopwatch stopwatch) {
        RpcCommand response = responseFuture.getResponse();
        if (response.getCode() != ResponseCode.GO_AWAY) {
            return CompletableFuture.completedFuture(responseFuture);
        }

        if (!config.isEnableReconnectForGoAway() || !config.isEnableTransparentRetry()) {
            return CompletableFuture.completedFuture(responseFuture);
        }

        ChannelWrapper channelWrapper = getChannelWrapper(responseFuture.getChannel());
        if (channelWrapper == null) {
            return CompletableFuture.completedFuture(responseFuture);
        }

        RpcCommand retryRequest = createRetryRequest(responseFuture.getRequest());
        if (channelWrapper.isOK()) {
            return retryInvokeWithOldChannel(responseFuture, stopwatch, channelWrapper, retryRequest);
        }

        return retryInvokeWithNewChannel(responseFuture, stopwatch, channelWrapper, retryRequest);
    }

    private CompletableFuture<ResponseFuture> retryInvokeWithOldChannel(ResponseFuture responseFuture, Stopwatch stopwatch, ChannelWrapper channelWrapper, RpcCommand retryRequest) {
        long duration = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        stopwatch.stop();
        Channel retryChannel = channelWrapper.getChannel();
        if (retryChannel != null && responseFuture.getChannel() != retryChannel) {
            long newTimeout = responseFuture.getTimeoutMillis() - duration;
            channelInvoker.invokeAsync(retryChannel, retryRequest, newTimeout);
        }
        return CompletableFuture.completedFuture(responseFuture);
    }

    private CompletableFuture<ResponseFuture> retryInvokeWithNewChannel(ResponseFuture responseFuture, Stopwatch stopwatch, ChannelWrapper channelWrapper, RpcCommand retryRequest) {
        CompletableFuture<ResponseFuture> future = new CompletableFuture<>();
        ChannelFuture channelFuture = channelWrapper.getChannelFuture();
        channelFuture.addListener(f -> {
            long duration = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.stop();
            if (!f.isSuccess()) {
                future.completeExceptionally(new RemotingConnectException(channelWrapper.getAddress()));
                return;
            }

            Channel retryChannel0 = channelFuture.channel();
            if (retryChannel0 == null || responseFuture.getChannel() == retryChannel0) {
                return;
            }
            long newTimeout = responseFuture.getTimeoutMillis() - duration;
            channelInvoker.invokeAsync(retryChannel0, retryRequest, newTimeout)
                .whenComplete((v, t) -> {
                    if (t != null) {
                        future.completeExceptionally(t);
                    } else {
                        future.complete(v);
                    }
                });
        });

        return future;
    }

    private RpcCommand createRetryRequest(RpcCommand request) {
        RpcCommand retryRequest = RpcCommand.createRequestCommand(request.getCode(), request.getCustomHeader());
        retryRequest.setBody(request.getBody());

        return retryRequest;

    }

    private ChannelWrapper getChannelWrapper(Channel channel) {
        return  channelMap.computeIfPresent(channel, (tmpChannel, tmpWrapper) -> {
            try {
                if (tmpWrapper.reconnect()) {
                    channelMap.put(tmpWrapper.getChannel(), tmpWrapper);
                }
            } catch (Throwable t) {
                log.error("Channel {} reconnect error", tmpWrapper, t);
            }

            return tmpWrapper;
        });
    }

    private boolean isSuccess(String addr, long timeout, RpcCallback rpcCallback, long costTime, ChannelFuture future, ChannelFuture f) {
        if (!f.isSuccess()) {
            rpcCallback.onFailure(new RemotingConnectException(addr));
            return false;
        }

        Channel channel = future.channel();
        String remoteAddr = NettyHelper.getRemoteAddr(channel);
        if (remoteAddr == null || !channel.isActive()) {
            this.closeChannel(addr, channel);
            rpcCallback.onFailure(new RemotingConnectException(addr));
            return false;
        }

        if (timeout < costTime) {
            rpcCallback.onFailure(new RemotingTooMuchRequestException("invokeAsync call the addr[" + remoteAddr + "] timeout"));
            return false;
        }

        return true;
    }

    private BiConsumer<ResponseFuture, Throwable> completeResponseFuture(CallbackWrapper callbackWrapper, RpcCommand request, long newTimeout, ChannelFuture future) {
        return (v, t) -> {
            if (t == null) {
                callbackWrapper.onComplete(v);
                return;
            }

            ResponseFuture responseFuture = new ResponseFuture(future.channel(), request.getOpaque(), request, newTimeout, null, null);
            responseFuture.setCause(t);
            callbackWrapper.onComplete(responseFuture);
        };
    }

    private ChannelFutureListener createInvokeFutureListener(String addr, RpcCommand request, long timeout, RpcCallback rpcCallback, long startTime, ChannelFuture future) {
        return f -> {
            long costTime = System.currentTimeMillis() - startTime;
            if (!isSuccess(addr, timeout, rpcCallback, costTime, future, f)) {
                return;
            }

            CallbackWrapper callbackWrapper = new CallbackWrapper(this, rpcCallback, addr);
            long newTimeout = timeout - costTime;
            this.invokeWithRetry(future.channel(), request, newTimeout)
                .whenComplete(
                    completeResponseFuture(callbackWrapper, request, newTimeout, future)
                ).thenAccept(responseFuture -> {
                    callbackWrapper.onSuccess(responseFuture.getResponse());
                }).exceptionally(t -> {
                    callbackWrapper.onFailure(t);
                    return null;
                }) ;
        };
    }

}
