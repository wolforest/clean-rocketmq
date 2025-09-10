package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.renew.DefaultReceiptHandler;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandler;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;

public class AckManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        MQFacade mqFacade = BrokerContext.getBean(MQStore.class);
        TopicFacade topicFacade = BrokerContext.getBean(TopicStore.class);
        ReceiptHandler receiptHandler = BrokerContext.getBean(DefaultReceiptHandler.class);

        AckService ackService = new AckService(brokerConfig, mqFacade, topicFacade, receiptHandler);
        BrokerContext.register(ackService);

        InvisibleService invisibleService = new InvisibleService(brokerConfig, mqFacade);
        BrokerContext.register(invisibleService);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
