package com.wolf.minimq.broker.server.grpc;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.broker.server.vo.BrokerContext;
import com.wolf.minimq.domain.config.BrokerConfig;
import com.wolf.minimq.domain.config.GrpcConfig;

public class GrpcManager implements Lifecycle {
    private GrpcConfig grpcConfig;
    private MessageManager messageManager;
    private MessageService messageService;
    private GrpcServer grpcServer;

    @Override
    public void initialize() {
        initConfig();
        initMessageService();
        initGrpcServer();
    }

    @Override
    public void start() {
        this.messageManager.start();
        this.grpcServer.start();
    }

    @Override
    public void shutdown() {
        this.messageManager.shutdown();
        this.grpcServer.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initConfig() {
        this.grpcConfig = BrokerContext.getBean(GrpcConfig.class);
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        this.grpcConfig.setGrpcPort(brokerConfig.getServerPort());
    }

    private void initMessageService() {
        this.messageManager = new MessageManager(grpcConfig);
        messageManager.initialize();
        this.messageService = this.messageManager.getMessageService();
    }

    private void initGrpcServer() {
        this.grpcServer = new GrpcServer(grpcConfig, messageService);
        this.grpcServer.initialize();
    }
}
