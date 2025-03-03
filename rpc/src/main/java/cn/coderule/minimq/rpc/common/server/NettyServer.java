package cn.coderule.minimq.rpc.common.server;

import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.core.NettyService;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;

public class NettyServer extends NettyService implements RpcServer {
    public NettyServer(int onewaySemaphore, int asyncSemaphore) {
        super(onewaySemaphore, asyncSemaphore);
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
