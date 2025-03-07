package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.model.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.RpcCallback;
import cn.coderule.minimq.rpc.common.core.model.RpcCommand;
import cn.coderule.minimq.rpc.common.core.RpcService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface RpcClient extends RpcService {
    boolean isChannelWritable(final String addr);
    boolean isAddressReachable(final String addr);
    void closeChannels(final List<String> addrList);
    default void closeChannel(final String addr) {
        closeChannels(List.of(addr));
    }

    void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor);

    RpcCommand invokeSync(final String addr, final RpcCommand request, final long timeoutMillis) throws Exception;
    void invokeAsync(final String addr, final RpcCommand request, final long timeoutMillis, final RpcCallback invokeCallback) throws Exception;
    void invokeOneway(final String addr, final RpcCommand request, final long timeoutMillis) throws Exception;

    default CompletableFuture<RpcCommand> invoke(final String addr, final RpcCommand request, final long timeoutMillis) {
        CompletableFuture<RpcCommand> future = new CompletableFuture<>();
        try {
            invokeAsync(addr, request, timeoutMillis, new RpcCallback() {
                @Override
                public void onComplete(ResponseFuture responseFuture) {
                }

                @Override
                public void onSuccess(RpcCommand response) {
                    future.complete(response);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }
}
