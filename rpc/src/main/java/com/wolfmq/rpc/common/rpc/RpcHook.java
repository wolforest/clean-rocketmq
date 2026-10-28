package com.wolfmq.rpc.common.rpc;

import com.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import com.wolfmq.rpc.common.rpc.core.invoke.RpcContext;

public interface RpcHook {
    void onRequestStart(RpcContext ctx, RpcCommand request);
    void onResponseComplete(RpcContext ctx, RpcCommand request, RpcCommand response);
}
