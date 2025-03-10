package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import cn.coderule.minimq.rpc.common.netty.service.NettyService;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import io.netty.channel.Channel;
import java.util.List;

public class NettyClient extends NettyService implements RpcClient {
    private final RpcClientConfig config;
    private final NettyEventExecutor nettyEventExecutor;

    public NettyClient(RpcClientConfig config, RpcListener rpcListener) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits(), config.getCallbackThreadNum());
        this.config = config;

        this.nettyEventExecutor = new NettyEventExecutor(rpcListener);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isChannelWritable(String addr) {
        return false;
    }

    @Override
    public boolean isAddressReachable(String addr) {
        return false;
    }

    @Override
    public void closeChannels(List<String> addrList) {

    }

    public void closeChannel(Channel channel) {

    }

    @Override
    public RpcCommand invokeSync(String addr, RpcCommand request,
        long timeoutMillis) throws Exception {
        return null;
    }

    @Override
    public void invokeAsync(String addr, RpcCommand request, long timeoutMillis,
        RpcCallback invokeCallback) throws Exception {

    }

    @Override
    public void invokeOneway(String addr, RpcCommand request, long timeoutMillis) throws Exception {

    }
}
