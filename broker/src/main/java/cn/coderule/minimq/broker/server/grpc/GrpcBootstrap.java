package cn.coderule.minimq.broker.server.grpc;

import cn.coderule.minimq.broker.server.grpc.service.MessageBootstrap;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.network.GrpcConfig;

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
