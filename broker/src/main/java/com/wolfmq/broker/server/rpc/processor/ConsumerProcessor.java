package com.wolfmq.broker.server.rpc.processor;

import com.wolfmq.rpc.common.rpc.RpcProcessor;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import com.wolfmq.rpc.common.rpc.core.invoke.RpcContext;

public class ConsumerProcessor implements RpcProcessor {
    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }
}
