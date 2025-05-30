
package cn.coderule.minimq.rpc.common.rpc.core.invoke;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class RequestTask implements Runnable {
    private final Runnable runnable;
    private final long createTimestamp = System.currentTimeMillis();
    private final Channel channel;
    private final RpcCommand request;
    private volatile boolean stopRun = false;

    public RequestTask(final Runnable runnable, final Channel channel, final RpcCommand request) {
        this.runnable = runnable;
        this.channel = channel;
        this.request = request;
    }

    @Override
    public void run() {
        if (!this.stopRun)
            this.runnable.run();
    }

}
