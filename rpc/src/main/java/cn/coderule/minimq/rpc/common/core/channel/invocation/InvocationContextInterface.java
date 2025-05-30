
package cn.coderule.minimq.rpc.common.core.channel.invocation;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;

public interface InvocationContextInterface {
    void handle(RpcCommand remotingCommand);

    boolean expired(long expiredTimeSec);
}
