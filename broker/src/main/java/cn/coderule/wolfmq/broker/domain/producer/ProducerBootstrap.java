package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.broker.api.ProducerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.broker.domain.transaction.Transaction;
import cn.coderule.wolfmq.broker.infra.store.MQStore;
import cn.coderule.wolfmq.broker.infra.store.TopicStore;
import cn.coderule.wolfmq.broker.server.bootstrap.BrokerContext;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;

public class ProducerBootstrap implements Lifecycle {
    private BrokerConfig brokerConfig;
    private EnqueueService enqueueService;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        initMessageSender();

        ProducerManager producerManager = new ProducerManager(brokerConfig);
        BrokerContext.register(producerManager);

        Producer producer = new Producer(enqueueService, producerManager);
        ProducerController controller = new ProducerController(brokerConfig, producer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() throws Exception {
        enqueueService.start();
    }

    @Override
    public void shutdown() throws Exception {
        enqueueService.shutdown();
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

        this.enqueueService = new EnqueueService(
            brokerConfig,
            hookManager,
            queueSelector,
            messageStore,
            topicStore,
            transaction
        );
    }


}
