package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;

public interface RpcHook {
    void onRequestStart(RpcContext ctx, RpcCommand request);
    void onResponseComplete(RpcContext ctx, RpcCommand request, RpcCommand response);
}
