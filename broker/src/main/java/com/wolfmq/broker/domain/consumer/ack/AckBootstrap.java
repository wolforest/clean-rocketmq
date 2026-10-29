package com.wolfmq.broker.domain.consumer.ack;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;

public class AckBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public AckBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
        MQFacade mqFacade = context.getBean(MQStore.class, ContextNamespace.APP);
        TopicFacade topicFacade = context.getBean(TopicStore.class, ContextNamespace.APP);
        ReceiptHandler receiptHandler = context.getBean(ReceiptHandler.class, ContextNamespace.APP);
        ConsumerManager consumerManager = context.getBean(ConsumerManager.class, ContextNamespace.APP);

        AckValidator ackValidator = new AckValidator(mqFacade, topicFacade);

        BrokerAckService ackService = new BrokerAckService(
            mqFacade, consumerManager, receiptHandler, ackValidator
        );
        context.register(ackService, ContextNamespace.APP);

        InvisibleService invisibleService = new InvisibleService(
            brokerConfig, mqFacade, consumerManager, receiptHandler, ackValidator
        );
        context.register(invisibleService, ContextNamespace.APP);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
