package cn.coderule.minimq.domain.config.network;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.domain.config.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class GrpcConfig implements Serializable {
    private int port = ConfigAttribute.GRPC_PORT;

    private int bossThreadNum = 1;
    private int workerThreadNum = SystemUtil.getProcessorNumber() * 2;
    private int businessThreadNum = 16 + SystemUtil.getProcessorNumber() * 2;
    private int businessQueueCapacity = 100000;
    private long shutdownTimeout = 30;

    private boolean enableEpoll = false;

    // 5 seconds
    private int requestTimeout = 5_000;
    private int maxConnectionIdle = 120_000;
    private int channelExpireTime = 30_000;
    private int contextExpireTime = 60_000;

    private int relayTimeout = 5_000;

    private int maxMessageSize = 4 * 1024 * 1024;
    private int maxInboundMessageSize = 130 * 1024 * 1024;

    private int routeThreadNum = SystemUtil.getProcessorNumber();
//    private int routeThreadNum = 1;
    private int routeQueueCapacity = 10000;
    private int producerThreadNum = SystemUtil.getProcessorNumber();
//    private int producerThreadNum = 1;
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
//    private int consumerThreadNum = 1;
    private int consumerQueueCapacity = 10000;
    private int clientThreadNum = SystemUtil.getProcessorNumber();
//    private int clientThreadNum = 1;
    private int clientQueueCapacity = 10000;
//    private int transactionThreadNum = 1;
    private int transactionThreadNum = SystemUtil.getProcessorNumber();
    private int transactionQueueCapacity = 10000;

    private boolean enableMessageTypeCheck = true;

    private int producerMaxAttempts = 3;
    private int producerBackoffMillis = 10;
    private int producerMaxBackoffMillis = 1000;
    private int producerBackoffMultiplier = 2;

    private int minConsumerPollTime = 5_000;
    private int maxConsumerPollTime = 20_000;
    private int batchConsumerPollSize = 32;




}
