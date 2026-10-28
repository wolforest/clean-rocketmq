package com.wolfmq.rpc.common.rpc;

import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import com.wolfmq.rpc.common.rpc.core.invoke.RpcContext;
import com.wolfmq.rpc.common.rpc.protocol.code.SystemResponseCode;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public interface RpcProcessor {
    RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException;
    boolean reject();

    default Collection<Integer> getCodeSet() {
        return Set.of();
    }

    default ExecutorService getExecutor() {
        return null;
    }

    default RpcCommand unsupportedCode(RpcContext ctx, RpcCommand request) {
        String error = " request type " + request.getCode() + " not supported";
        return RpcCommand.createResponseCommand(SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
    }

}
