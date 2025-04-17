package cn.coderule.minimq.rpc.rpc;

import cn.coderule.minimq.rpc.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.rpc.core.invoke.RpcContext;

public interface RpcHook {
    void onRequestStart(RpcContext ctx, RpcCommand request);
    void onResponseComplete(RpcContext ctx, RpcCommand request, RpcCommand response);
}
