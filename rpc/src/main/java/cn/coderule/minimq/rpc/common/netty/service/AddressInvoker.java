package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AddressInvoker {
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

    public RpcCommand invokeSync(String addr, RpcCommand request,
        long timeoutMillis) throws Exception {
        return null;
    }

    public void invokeAsync(String addr, RpcCommand request, long timeoutMillis,
        RpcCallback invokeCallback) throws Exception {

    }

    public void invokeOneway(String addr, RpcCommand request, long timeoutMillis) throws Exception {

    }

    public boolean isChannelWritable(String addr) {
        return false;
    }

    public boolean isAddressReachable(String addr) {
        return false;
    }

    public void closeChannel(Channel channel) {

    }

    public void closeChannels(List<String> addrList) {

    }

}
