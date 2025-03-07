package cn.coderule.minimq.rpc.common.core;

import cn.coderule.minimq.rpc.common.core.model.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.model.RpcCommand;

public interface RpcCallback {

    void onComplete(ResponseFuture responseFuture);
    default void onSuccess(final RpcCommand response) {}
    default void onFailure(final Throwable throwable) {}
}
