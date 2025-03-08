package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;

public interface RpcProcessor {
    RpcCommand process(RpcContext ctx, RpcCommand request);
    boolean reject();
}
