package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.consumer.InflightCounter;
import cn.coderule.minimq.broker.domain.consumer.renew.DefaultReceiptHandler;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.broker.infra.store.ConsumeOrderStore;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class PopManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private QueueSelector queueSelector;
    private ContextBuilder contextBuilder;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        initContextBuilder();
        initQueueSelector();
        initPopService();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    private void initPopService() {
        PopService popService = new PopService(
            brokerConfig,
            BrokerContext.getBean(InflightCounter.class),
            queueSelector,
            BrokerContext.getBean(MQStore.class),
            contextBuilder,
            BrokerContext.getBean(DefaultReceiptHandler.class),
            BrokerContext.getBean(ConsumeOrderStore.class)
        );

        BrokerContext.register(popService);
    }

    private void initContextBuilder() {
        contextBuilder = new ContextBuilder(
            brokerConfig,
            BrokerContext.getBean(ConsumerRegister.class),
            BrokerContext.getBean(TopicStore.class),
            BrokerContext.getBean(SubscriptionStore.class)
        );
    }

    private void initQueueSelector() {
        queueSelector = new QueueSelector(
            BrokerContext.getBean(RouteService.class)
        );
    }

}
