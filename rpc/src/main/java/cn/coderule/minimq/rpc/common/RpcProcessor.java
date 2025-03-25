package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
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

}
