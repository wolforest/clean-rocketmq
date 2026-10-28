
package com.wolfmq.rpc.common.core.channel.invocation;

import com.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;

public interface InvocationContextInterface {
    void handle(RpcCommand remotingCommand);

    boolean expired(long expiredTimeSec);
}
