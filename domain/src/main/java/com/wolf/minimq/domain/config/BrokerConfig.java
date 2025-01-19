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
    private int grpcThreadPoolNums = 16 + PROCESSOR_NUMBER * 2;
    private int grpcThreadPoolQueueCapacity = 100000;

}
