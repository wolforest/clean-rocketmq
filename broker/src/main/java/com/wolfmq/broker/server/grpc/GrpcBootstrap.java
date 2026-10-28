package com.wolfmq.broker.server.grpc;

import com.wolfmq.broker.server.grpc.service.MessageBootstrap;
import com.wolfmq.broker.server.bootstrap.BrokerContext;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.config.network.GrpcConfig;

public class GrpcBootstrap implements Lifecycle {
    private BrokerConfig brokerConfig;
    private GrpcConfig grpcConfig;
    private MessageBootstrap messageBootstrap;
    private GrpcMessageService messageService;
    private GrpcServer grpcServer;

    @Override
    public void initialize() throws Exception {
        initConfig();
        initMessageService();
        initGrpcServer();
    }

    @Override
    public void start() throws Exception {
        this.messageBootstrap.start();
        this.grpcServer.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.messageBootstrap.shutdown();
        this.grpcServer.shutdown();
    }

    private void initConfig() {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        this.grpcConfig = brokerConfig.getGrpcConfig();

        this.grpcConfig.setPort(brokerConfig.getPort());
    }

    private void initMessageService() {
        this.messageBootstrap = new MessageBootstrap(brokerConfig);
        try {
            messageBootstrap.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.messageService = this.messageBootstrap.getMessageService();
    }

    private void initGrpcServer() {
        this.grpcServer = new GrpcServer(grpcConfig, messageService);
        try {
            this.grpcServer.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
