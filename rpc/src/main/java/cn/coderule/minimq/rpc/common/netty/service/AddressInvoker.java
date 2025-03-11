package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.core.exception.RemotingConnectException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    private ChannelFuture getOrCreateChannelAsync(String addr) {
        return null;
    }

    private Channel getOrCreateChannel(String addr) {
        return null;
    }

    public RpcCommand invokeSync(String addr, RpcCommand request, long timeout) throws Exception {
        long startTime = System.currentTimeMillis();
        Channel channel = getOrCreateChannel(addr);
        String remoteAddr = NettyHelper.getRemoteAddr(channel);

        if (channel == null || !channel.isActive()) {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }

        long leftTime = timeout;
        try {
            long costTime = System.currentTimeMillis() - startTime;
            leftTime -= costTime;
            if (leftTime <= 0) {
                throw new RemotingTimeoutException(addr, timeout);
            }
            RpcCommand response = this.invokeWithRetry(channel, request, leftTime)
                .thenApply(ResponseFuture::getResponse)
                .get(leftTime, TimeUnit.MILLISECONDS);

            updateChannelLastResponseTime(addr);
            return response;
        } catch (ExecutionException e) {
            log.warn("invokeSync: send request exception, so close the channel[{}]", remoteAddr);
            this.closeChannel(addr, channel);
            throw e;
        } catch (RemotingTimeoutException e) {
            // avoid close the success channel if left timeout is small,
            // since it may cost too much time in get the success channel, the left timeout for read is small
            boolean shouldClose = leftTime > MIN_CLOSE_TIMEOUT_MILLIS || leftTime > timeout / 4;
            if (shouldClose && config.isCloseChannelWhenTimeout()) {
                this.closeChannel(addr, channel);
                log.warn("invokeSync: close socket because of timeout, {}ms, {}", timeout, remoteAddr);
            }

            log.warn("invokeSync: wait response timeout exception, the channel[{}]", remoteAddr);
            throw e;
        }
    }

    public void invokeAsync(String addr, RpcCommand request, long timeout, RpcCallback rpcCallback) throws Exception {
        long startTime = System.currentTimeMillis();
        ChannelFuture future = getOrCreateChannelAsync(addr);
        if (future == null) {
            rpcCallback.onFailure(new RemotingConnectException(addr));
            return;
        }

        future.addListener(f -> {
            if (!f.isSuccess()) {
                rpcCallback.onFailure(new RemotingConnectException(addr));
                return;
            }

            Channel channel = future.channel();
            String remoteAddr = NettyHelper.getRemoteAddr(channel);
            if (remoteAddr == null || !channel.isActive()) {
                this.closeChannel(addr, channel);
                rpcCallback.onFailure(new RemotingConnectException(addr));
                return;
            }

            long costTime = System.currentTimeMillis() - startTime;
            if (timeout < costTime) {
                rpcCallback.onFailure(new RemotingTooMuchRequestException("invokeAsync call the addr[" + remoteAddr + "] timeout"));
                return;
            }
            CallbackWrapper callbackWrapper = new CallbackWrapper(this, rpcCallback, addr);
            long newTimeout = timeout - costTime;
            this.invokeWithRetry(channel, request, newTimeout)
                .whenComplete((v, t) -> {
                    if (t == null) {
                        callbackWrapper.onComplete(v);
                    } else {
                        ResponseFuture responseFuture = new ResponseFuture(channel, request.getOpaque(), request, newTimeout, null, null);
                        responseFuture.setCause(t);
                        callbackWrapper.onComplete(responseFuture);
                    }
                })
                .thenAccept(responseFuture -> {
                    callbackWrapper.onSuccess(responseFuture.getResponse());
                })
                .exceptionally(t -> {
                    callbackWrapper.onFailure(t);
                    return null;
                }) ;
        });
    }

    public CompletableFuture<RpcCommand> invokeAsync(String addr, RpcCommand request, long timeout) {
        return null;
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
            long duration = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.stop();
            Channel retryChannel = channelWrapper.getChannel();
            if (retryChannel != null && responseFuture.getChannel() != retryChannel) {
                long newTimeout = responseFuture.getTimeoutMillis() - duration;
                channelInvoker.invokeAsync(retryChannel, retryRequest, newTimeout);
            }
            return CompletableFuture.completedFuture(responseFuture);
        }

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

    public ChannelWrapper getChannelWrapper(Channel channel) {
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

    public Bootstrap getBootstrap(String addr) {
        return bootstrap;
    }

    public void updateChannelLastResponseTime(String addr) {

    }

    public boolean isChannelWritable(String addr) {
        return false;
    }

    public boolean isAddressReachable(String addr) {
        return false;
    }

    public void closeChannel(final String addr, final Channel channel) {

    }

    public void closeChannel(Channel channel) {

    }

    public void closeChannels(List<String> addrList) {

    }

}
