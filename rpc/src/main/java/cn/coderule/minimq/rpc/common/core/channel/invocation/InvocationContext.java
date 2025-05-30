
package cn.coderule.minimq.rpc.common.core.channel.invocation;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class InvocationContext implements InvocationContextInterface {
    private final CompletableFuture<RpcCommand> response;
    private final long timestamp = System.currentTimeMillis();

    public InvocationContext(CompletableFuture<RpcCommand> resp) {
        this.response = resp;
    }

    public boolean expired(long expiredTimeSec) {
        return System.currentTimeMillis() - timestamp >= Duration.ofSeconds(expiredTimeSec).toMillis();
    }

    public CompletableFuture<RpcCommand> getResponse() {
        return response;
    }

    public void handle(RpcCommand RpcCommand) {
        response.complete(RpcCommand);
    }
}
