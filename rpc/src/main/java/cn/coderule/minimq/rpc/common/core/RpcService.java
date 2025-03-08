package cn.coderule.minimq.rpc.common.core;

import cn.coderule.minimq.rpc.common.core.invoke.RpcHook;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import java.util.concurrent.ExecutorService;

public interface RpcService {
    void start();
    void shutdown();

    void registerRpcHook(RpcHook rpcHook);
    void clearRpcHook();

    ExecutorService getProcessorExecutor();
    RpcListener getRpcListener();
}
