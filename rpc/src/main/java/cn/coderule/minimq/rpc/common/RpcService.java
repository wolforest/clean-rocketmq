package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;

public interface RpcService {
    void start();
    void shutdown();

    void registerRpcHook(RpcHook rpcHook);
    void clearRpcHook();

    void setRpcPipeline(RpcPipeline pipeline);
}
