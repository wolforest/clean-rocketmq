package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.renew.DefaultReceiptHandler;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandler;

public class PopManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private QueueSelector queueSelector;
    private DequeueService dequeueService;
    private ContextBuilder contextBuilder;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        initContextBuilder();
        initQueueSelector();
        initDequeueService();
        initPopService();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
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
            brokerConfig,
            BrokerContext.getBean(RouteService.class)
        );
    }

    private void initDequeueService() {
        dequeueService = new DequeueService(
            BrokerContext.getBean(MQStore.class)
        );
    }

    private void initPopService() {
        ReceiptHandler receiptHandler = BrokerContext.getBean(DefaultReceiptHandler.class);
        PopService popService = new PopService(
            contextBuilder, queueSelector,
            dequeueService, receiptHandler
        );

        BrokerContext.register(popService);
    }

}
