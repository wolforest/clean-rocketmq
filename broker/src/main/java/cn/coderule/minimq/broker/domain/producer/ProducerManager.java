package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.broker.domain.transaction.Transaction;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class ProducerManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private MessageSender messageSender;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        initMessageSender();

        ProducerRegister producerRegister = new ProducerRegister(brokerConfig);
        BrokerContext.register(producerRegister);

        Producer producer = new Producer(messageSender, producerRegister);
        ProducerController controller = new ProducerController(brokerConfig, producer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() throws Exception {
        messageSender.start();
    }

    @Override
    public void shutdown() throws Exception {
        messageSender.shutdown();
    }

    private void initMessageSender() {
        ProduceHookManager hookManager = new ProduceHookManager();
        BrokerContext.register(hookManager);

        RouteService routeService = BrokerContext.getBean(RouteService.class);
        QueueSelector queueSelector = new QueueSelector(routeService);
        BrokerContext.register(queueSelector);

        MQStore messageStore = BrokerContext.getBean(MQStore.class);
        TopicStore topicStore = BrokerContext.getBean(TopicStore.class);
        Transaction transaction = BrokerContext.getBean(Transaction.class);

        this.messageSender = new MessageSender(
            brokerConfig,
            hookManager,
            queueSelector,
            messageStore,
            topicStore,
            transaction
        );
    }


}
