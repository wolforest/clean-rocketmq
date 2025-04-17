package cn.coderule.minimq.rpc.common.rpc;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;

public interface RpcHook {
    void onRequestStart(RpcContext ctx, RpcCommand request);
    void onResponseComplete(RpcContext ctx, RpcCommand request, RpcCommand response);
}
