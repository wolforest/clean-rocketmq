package cn.coderule.minimq.domain.config.network;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class RpcConfig implements Serializable {
    private String host = NetworkUtil.getLocalAddress();
    private int port = ConfigAttribute.RPC_PORT;

    private int bossThreadNum = 1;
    private int workerThreadNum = 3;
    private int businessThreadNum = 8;
    private int callbackThreadNum = 0;


    private int heartbeatThreadNum = 2 * SystemUtil.getProcessorNumber();
    private int heartbeatQueueCapacity = 50000;
    private long heartbeatWaitTime = 31_000;

    private int routeThreadNum = 2 * SystemUtil.getProcessorNumber();
    private int routeQueueCapacity = 50000;
    private long routeQueueWaitTime = 3_000;

    private int producerThreadNum = 4 * SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private long producerQueueWaitTime = 3_000;

    private int consumerThreadNum = 4 * SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 50000;
    private long consumerQueueWaitTime = 5_000;

    private int offsetThreadNum = 4 * SystemUtil.getProcessorNumber();
    private int offsetQueueCapacity = 10000;
    private long offsetQueueWaitTime = 3_000;

    private int defaultThreadNum = 4 * SystemUtil.getProcessorNumber();
    private int defaultQueueCapacity = 50000;
    private long defaultQueueWaitTime = 3_000;

}
