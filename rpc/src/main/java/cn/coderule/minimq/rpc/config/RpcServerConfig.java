package cn.coderule.minimq.rpc.config;

import java.io.Serializable;
import lombok.Data;

@Data
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
    private int processorThreadNum = 0;

    private boolean useEpoll = false;
    private int onewaySemaphorePermits = 256;
    private int asyncSemaphorePermits = 64;
    private int maxChannelIdle = 120_000;

    private int sendBufferSize = 0;
    private int receiveBufferSize = 0;
    private int writeBufferHighWater = 0;
    private int writeBufferLowWater = 0;
    private int socketBacklog = 1024;
    private boolean enableNettyPool = true;

    private boolean enableGracefullyShutdown = false;
    private int shutdownWaitTime = 30_000;

}
