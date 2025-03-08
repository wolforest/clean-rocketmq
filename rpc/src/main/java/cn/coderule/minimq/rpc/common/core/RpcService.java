package cn.coderule.minimq.rpc.common.core;

import cn.coderule.minimq.rpc.common.RpcHook;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import java.util.concurrent.ExecutorService;

public interface RpcService {
    void start();
    void shutdown();

    void registerRpcHook(RpcHook rpcHook);
    void clearRpcHook();
    RpcListener getRpcListener();

    void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor);
    ExecutorService getCallbackExecutor();
}
