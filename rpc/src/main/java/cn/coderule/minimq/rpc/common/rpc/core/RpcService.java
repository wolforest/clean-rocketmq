package cn.coderule.minimq.rpc.common.rpc.core;

import cn.coderule.minimq.rpc.common.rpc.RpcHook;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

public interface RpcService {
    void start();
    void shutdown();

    void registerRpcHook(RpcHook rpcHook);
    void clearRpcHook();

    void registerProcessor(RpcProcessor processor);

    /**
     * to be deleted
     */
    void registerProcessor(Collection<Integer> codes, RpcProcessor processor, ExecutorService executor);
    void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor);
    void registerDefaultProcessor(RpcProcessor processor, ExecutorService executor);
}
