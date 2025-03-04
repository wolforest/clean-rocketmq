package cn.coderule.minimq.rpc.common.core;

public interface RpcCallback {

    void onComplete(ResponseFuture responseFuture);
    default void onSuccess(final RpcCommand response) {}
    default void onFailure(final Throwable throwable) {}
}
