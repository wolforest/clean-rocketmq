package cn.coderule.minimq.rpc.common.rpc.core.invoke;

public interface RpcCallback {

    void onComplete(ResponseFuture responseFuture);
    default void onSuccess(final RpcCommand response) {}
    default void onFailure(final Throwable throwable) {}
}
