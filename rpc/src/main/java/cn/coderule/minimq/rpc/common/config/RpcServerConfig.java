package cn.coderule.minimq.rpc.common.config;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcServerConfig implements Serializable {
    /**
     * Bind address may be hostname, IPv4 or IPv6.
     * By default, it's wildcard address, listening all network interfaces.
     */
    private String address = "0.0.0.0";
    private int port = 0;

    private int bossThreadNum = 1;
    private int workerThreadNum = 3;
    private int businessThreadNum = 8;
    private int callbackThreadNum = 0;

    private boolean useEpoll = false;
    private int onewaySemaphorePermits = 256;
    private int asyncSemaphorePermits = 64;
    private int maxChannelIdle = 120;

    private int sendBufferSize = 0;
    private int receiveBufferSize = 0;
    private int writeBufferHighWater = 0;
    private int writeBufferLowWater = 0;
    private int socketBacklog = 1024;
    private boolean enableNettyPool = true;

    private boolean enableGracefullyShutdown = false;
    private int shutdownWaitTime = 30_000;

}
