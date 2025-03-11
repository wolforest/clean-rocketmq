package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;

public class CallbackWrapper implements RpcCallback {
    private final AddressInvoker invoker;
    private final RpcCallback rpcCallback;
    private final String addr;

    public CallbackWrapper(AddressInvoker invoker, RpcCallback rpcCallback, String addr) {
        this.invoker = invoker;
        this.rpcCallback = rpcCallback;
        this.addr = addr;
    }

    @Override
    public void onComplete(ResponseFuture responseFuture) {

    }

    @Override
    public void onSuccess(RpcCommand response) {
        RpcCallback.super.onSuccess(response);
    }

    @Override
    public void onFailure(Throwable throwable) {
        RpcCallback.super.onFailure(throwable);
    }
}
