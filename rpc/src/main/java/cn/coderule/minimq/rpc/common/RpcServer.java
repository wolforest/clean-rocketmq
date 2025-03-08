package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.RpcService;
import io.netty.channel.Channel;
import java.util.concurrent.ExecutorService;

public interface RpcServer extends RpcService {
    void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor);

    RpcCommand invokeSync(Channel channel, RpcCommand request, long timeoutMillis) throws Exception;
    void invokeAsync(Channel channel, RpcCommand request, long timeoutMillis, RpcCallback callback) throws Exception;
    void invokeOneway(Channel channel, RpcCommand request, long timeoutMillis) throws Exception;
}
