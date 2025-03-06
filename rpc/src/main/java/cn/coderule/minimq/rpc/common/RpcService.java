package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;
import java.util.concurrent.ExecutorService;

public interface RpcService {
    void start();
    void shutdown();

    void registerRpcHook(RpcHook rpcHook);
    void clearRpcHook();

    ExecutorService getProcessorExecutor();
    RpcListener getRpcListener();
    void setRpcPipeline(RpcPipeline pipeline);
}
