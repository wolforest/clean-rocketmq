package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.consumer.InflightCounter;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.broker.infra.store.ConsumeOrderStore;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class PopManager implements Lifecycle {

    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        DefaultReceiptHandler receiptHandler = new DefaultReceiptHandler();
        QueueSelector queueSelector = new QueueSelector(
            BrokerContext.getBean(RouteService.class)
        );

        PopContextBuilder contextBuilder = new PopContextBuilder(
            brokerConfig,
            BrokerContext.getBean(ConsumerRegister.class),
            BrokerContext.getBean(TopicStore.class),
            BrokerContext.getBean(SubscriptionStore.class)
        );

        PopService popService = new PopService(
            brokerConfig,
            BrokerContext.getBean(InflightCounter.class),
            queueSelector,
            BrokerContext.getBean(MQStore.class),
            contextBuilder,
            receiptHandler,
            BrokerContext.getBean(ConsumeOrderStore.class)
        );

        BrokerContext.register(popService);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
