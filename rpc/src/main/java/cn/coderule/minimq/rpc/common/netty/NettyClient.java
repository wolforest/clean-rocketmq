package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.core.RpcCallback;
import cn.coderule.minimq.rpc.common.core.model.RpcCommand;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.netty.service.NettyService;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import java.util.List;
import java.util.concurrent.ExecutorService;
import lombok.Getter;

public class NettyClient extends NettyService implements RpcClient {
    private final RpcClientConfig config;

    @Getter
    private RpcListener rpcListener;
    @Getter
    private ExecutorService processorExecutor;

    public NettyClient(RpcClientConfig config) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits());
        this.config = config;
    }

    @Override
    public RpcListener getRpcListener() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerRpcHook(RpcHook rpcHook) {

    }

    @Override
    public void clearRpcHook() {

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

    @Override
    public void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor) {

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
