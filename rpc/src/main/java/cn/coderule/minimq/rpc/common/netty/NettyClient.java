package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;
import cn.coderule.minimq.rpc.config.RpcClientConfig;

public class NettyClient extends NettyService implements RpcClient {
    private final RpcClientConfig config;

    public NettyClient(RpcClientConfig config) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits());
        this.config = config;
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
    public void setRpcPipeline(RpcPipeline pipeline) {

    }
}
