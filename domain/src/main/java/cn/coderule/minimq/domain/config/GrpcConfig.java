package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.lang.SystemUtil;
import java.io.Serializable;
import lombok.Data;

@Data
public class GrpcConfig implements Serializable {
    private int port = 8081;

    private int bossThreadNum = 1;
    private int workerThreadNum = SystemUtil.getProcessorNumber() * 2;
    private int businessThreadNum = 16 + SystemUtil.getProcessorNumber() * 2;
    private int businessQueueCapacity = 100000;
    private long shutdownTimeout = 30;

    private boolean enableEpoll = false;

    // 5 seconds
    private int requestTimeout = 5000;
    private int maxConnectionIdle = 120 * 1000;

    private int maxMessageSize = 4 * 1024 * 1024;
    private int maxInboundMessageSize = 130 * 1024 * 1024;

    private int routeThreadNum = SystemUtil.getProcessorNumber();
    private int routeQueueCapacity = 10000;
    private int producerThreadNum = SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 10000;
    private int clientThreadNum = SystemUtil.getProcessorNumber();
    private int clientQueueCapacity = 10000;
    private int transactionThreadNum = SystemUtil.getProcessorNumber();
    private int transactionQueueCapacity = 10000;




}
