package com.wolf.minimq.domain.config;

import com.wolf.common.util.lang.SystemUtil;
import java.io.Serializable;
import lombok.Data;

@Data
public class GrpcConfig implements Serializable {
    private boolean enableGrpcEpoll = false;

    private int grpcPort = 8081;

    private long grpcShutdownTimeout = 30;
    private int grpcBossThreadNum = 1;
    private int grpcWorkerThreadNum = SystemUtil.getProcessorNumber() * 2;
    private int grpcBusinessThreadNum = 16 + SystemUtil.getProcessorNumber() * 2;
    private int grpcBusinessQueueCapacity = 100000;

    // 5 seconds
    private int grpcRequestTimeout = 5000;
    private int maxConnectionIdle = 120 * 1000;

    private int maxMessageSize = 4 * 1024 * 1024;
    private int maxInboundMessageSize = 130 * 1024 * 1024;

    private int grpcRouteThreadNum = SystemUtil.getProcessorNumber();
    private int grpcRouteQueueCapacity = 10000;
    private int grpcProducerThreadNum = SystemUtil.getProcessorNumber();
    private int grpcProducerQueueCapacity = 10000;
    private int grpcConsumerThreadNum = SystemUtil.getProcessorNumber();
    private int grpcConsumerQueueCapacity = 10000;
    private int grpcClientThreadNum = SystemUtil.getProcessorNumber();
    private int grpcClientQueueCapacity = 10000;
    private int grpcTransactionThreadNum = SystemUtil.getProcessorNumber();
    private int grpcTransactionQueueCapacity = 10000;




}
