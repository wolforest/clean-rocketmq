package com.wolfmq.broker.domain.producer;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.broker.api.ProducerController;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.meta.RouteService;
import com.wolfmq.broker.domain.transaction.Transaction;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.domain.config.server.BrokerConfig;

public class ProducerBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public ProducerBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    private EnqueueService enqueueService;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);

        initMessageSender();

        ProducerManager producerManager = new ProducerManager(brokerConfig);
        context.register(producerManager, ContextNamespace.APP);

        Producer producer = new Producer(enqueueService, producerManager);
        ProducerController controller = new ProducerController(brokerConfig, producer);
        context.register(controller, ContextNamespace.API);
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
        context.register(hookManager, ContextNamespace.APP);

        RouteService routeService = context.getBean(RouteService.class, ContextNamespace.APP);
        QueueSelector queueSelector = new QueueSelector(routeService);
        context.register(queueSelector, ContextNamespace.APP);

        MQStore messageStore = context.getBean(MQStore.class, ContextNamespace.APP);
        TopicStore topicStore = context.getBean(TopicStore.class, ContextNamespace.APP);
        Transaction transaction = context.getBean(Transaction.class, ContextNamespace.APP);

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
