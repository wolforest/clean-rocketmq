package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;

public interface RpcHook {
    void beforeRequest(RpcContext ctx, RpcCommand request);
    void afterResponse(RpcContext ctx, RpcCommand request, RpcCommand response);
}
