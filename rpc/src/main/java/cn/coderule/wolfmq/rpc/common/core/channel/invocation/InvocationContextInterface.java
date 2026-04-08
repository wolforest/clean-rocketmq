
package cn.coderule.wolfmq.rpc.common.core.channel.invocation;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;

public interface InvocationContextInterface {
    void handle(RpcCommand remotingCommand);

    boolean expired(long expiredTimeSec);
}
