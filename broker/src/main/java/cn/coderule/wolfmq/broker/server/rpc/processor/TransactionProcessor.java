package cn.coderule.wolfmq.broker.server.rpc.processor;

import cn.coderule.wolfmq.rpc.common.rpc.RpcProcessor;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcContext;

public class TransactionProcessor implements RpcProcessor {
    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }
}
