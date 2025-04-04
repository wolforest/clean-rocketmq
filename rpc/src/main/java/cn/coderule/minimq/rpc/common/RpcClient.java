package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.RpcService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RpcClient extends RpcService {
    boolean isChannelWritable(final String addr);
    boolean isAddressReachable(final String addr);

    void closeChannel(Channel channel);
    void closeChannels(final List<String> addrList);
    default void closeChannel(final String addr) {
        closeChannels(List.of(addr));
    }

    ChannelFuture getOrCreateChannelAsync(String addr) throws InterruptedException;
    Channel getOrCreateChannel(final String addr) throws InterruptedException;

    RpcCommand invokeSync(final String addr, final RpcCommand request, final long timeoutMillis) throws Exception;
    CompletableFuture<RpcCommand> invokeASync(final String addr, final RpcCommand request, final long timeoutMillis) throws Exception;
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
