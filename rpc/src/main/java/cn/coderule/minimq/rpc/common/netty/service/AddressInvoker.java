package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.core.exception.RemotingConnectException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTooMuchRequestException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private final ChannelInvoker channelInvoker;

    private final Lock lock = new ReentrantLock();
    private final ConcurrentMap<String /* addr */, ChannelWrapper> addressMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, ChannelWrapper> channelMap = new ConcurrentHashMap<>();

    public AddressInvoker(RpcClientConfig config, Bootstrap bootstrap, ChannelInvoker channelInvoker) {
        this.config = config;
        this.bootstrap = bootstrap;
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
            RpcCommand response = channelInvoker.invokeSync(channel, request, leftTime);
            updateChannelLastResponseTime(addr);

            return response;
        } catch (RemotingSendRequestException e) {
            log.warn("invokeSync: send request exception, so close the channel[{}]", remoteAddr);
            this.closeChannel(addr, channel);
            throw e;
        } catch (RemotingTimeoutException e) {
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
            channelInvoker.invokeAsync(channel, request, timeout - costTime, callbackWrapper);
        });
    }

    public void invokeOneway(String addr, RpcCommand request, long timeout) throws Exception {

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
