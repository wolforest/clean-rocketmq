package cn.coderule.minimq.rpc.common.server;

import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.core.NettyService;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;
import cn.coderule.minimq.rpc.config.RpcServerConfig;

public class NettyServer extends NettyService implements RpcServer {
    private final RpcServerConfig config;

    public NettyServer(RpcServerConfig config) {
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
