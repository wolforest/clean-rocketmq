package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;

public interface RpcProcessor {
    RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException;
    boolean reject();
}
