package com.wolfmq.broker.domain.consumer.ack;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.broker.server.bootstrap.BrokerContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;

public class AckBootstrap implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        MQFacade mqFacade = BrokerContext.getBean(MQStore.class);
        TopicFacade topicFacade = BrokerContext.getBean(TopicStore.class);
        ReceiptHandler receiptHandler = BrokerContext.getBean(ReceiptHandler.class);
        ConsumerManager consumerManager = BrokerContext.getBean(ConsumerManager.class);

        AckValidator ackValidator = new AckValidator(mqFacade, topicFacade);

        BrokerAckService ackService = new BrokerAckService(
            mqFacade, consumerManager, receiptHandler, ackValidator
        );
        BrokerContext.register(ackService);

        InvisibleService invisibleService = new InvisibleService(
            brokerConfig, mqFacade, consumerManager, receiptHandler, ackValidator
        );
        BrokerContext.register(invisibleService);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
