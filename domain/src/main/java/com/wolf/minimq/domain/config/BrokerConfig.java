package com.wolf.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class BrokerConfig implements Serializable {
    private static final int PROCESSOR_NUMBER = Runtime.getRuntime().availableProcessors();

    private Integer grpcPort = 8081;
    private boolean enableGrpcEpoll = false;

    private long grpcShutdownTimeout = 30;
    private int grpcBossThreadNum = 1;
    private int grpcWorkerThreadNum = PROCESSOR_NUMBER * 2;
    private int grpcBusinessThreadNum = 16 + PROCESSOR_NUMBER * 2;
    private int grpcBusinessQueueCapacity = 100000;

    // 5 seconds
    private int grpcRequestTimeout = 5000;

    private int routeThreadNum = PROCESSOR_NUMBER;
    private int routeQueueCapacity = 10000;
    private int producerThreadNum = PROCESSOR_NUMBER;
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = PROCESSOR_NUMBER;
    private int consumerQueueCapacity = 10000;
    private int clientThreadNum = PROCESSOR_NUMBER;
    private int clientQueueCapacity = 10000;
    private int transactionThreadNum = PROCESSOR_NUMBER;
    private int transactionQueueCapacity = 10000;

    private int producerCallbackThreadNum = PROCESSOR_NUMBER;
    private int producerCallbackQueueCapacity = 10000;
    private int consumerCallbackThreadNum = PROCESSOR_NUMBER;
    private int consumerCallbackQueueCapacity = 10000;


}
