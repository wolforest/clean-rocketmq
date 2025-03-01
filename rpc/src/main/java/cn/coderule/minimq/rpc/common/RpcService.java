package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.rpc.RpcHook;
import cn.coderule.minimq.rpc.common.rpc.RpcPipeline;

public interface RpcService {
    void start();
    void shutdown();

    void registerRpcHook(RpcHook rpcHook);
    void clearRpcHook();

    void setRpcPipeline(RpcPipeline pipeline);
}
