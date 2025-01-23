package com.wolf.minimq.broker.server.grpc;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.broker.server.vo.BrokerContext;
import com.wolf.minimq.domain.config.NetworkConfig;

public class GrpcManager implements Lifecycle {
    private NetworkConfig networkConfig;
    private MessageManager messageManager;
    private MessageService messageService;

    @Override
    public void initialize() {
        this.networkConfig = BrokerContext.getBean(NetworkConfig.class);
        initMessageService();

    }



    @Override
    public void start() {
        this.messageManager.start();
    }

    @Override
    public void shutdown() {
        this.messageManager.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initMessageService() {
        this.messageManager = new MessageManager(networkConfig);
        messageManager.initialize();
        this.messageService = this.messageManager.getMessageService();
    }
}
