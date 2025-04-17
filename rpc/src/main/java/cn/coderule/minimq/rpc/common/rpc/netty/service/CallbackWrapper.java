package cn.coderule.minimq.rpc.common.rpc.netty.service;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;

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
        this.rpcCallback.onComplete(responseFuture);
    }

    @Override
    public void onSuccess(RpcCommand response) {
        this.invoker.updateLastResponseTime(addr);
        this.rpcCallback.onSuccess(response);
    }

    @Override
    public void onFailure(Throwable throwable) {
        this.rpcCallback.onFailure(throwable);
    }
}
