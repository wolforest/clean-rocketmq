package cn.coderule.minimq.rpc.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class RpcClientConfig implements Serializable {
    private int workerThreadNum = 4;
    private int callbackThreadNum = Runtime.getRuntime().availableProcessors();
    private int onewaySemaphorePermits = 65535;
    private int asyncSemaphorePermits = 65535;

    private int connectTimeout = 3_000;
    private long maxChannelIdle = 120_000;
    private long idleCheckInterval = 60_000;
    private long maxReconnectTimeout = 60_000;

    private boolean enableNettyPool = false;
    private boolean enableTransparentRetry = true;
    private boolean disableCallbackExecutor = false;
    private boolean disableNettyWorkerGroup = false;
    private boolean closeChannelWhenTimeout = true;
    private boolean enableTLS = false;
    private String proxyConfig = "{}";

    private int sendBufferSize = 0;
    private int receiveBufferSize = 0;
    private int writeBufferHighWater = 0;
    private int writeBufferLowWater = 0;
}
