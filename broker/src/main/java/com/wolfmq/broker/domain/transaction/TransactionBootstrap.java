package com.wolfmq.broker.domain.transaction;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.broker.api.TransactionController;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.producer.ProducerManager;
import com.wolfmq.broker.domain.transaction.check.service.CheckService;
import com.wolfmq.broker.domain.transaction.check.CheckerFactory;
import com.wolfmq.broker.domain.transaction.check.service.DiscardService;
import com.wolfmq.broker.domain.transaction.check.context.TransactionContext;
import com.wolfmq.broker.domain.transaction.receipt.ReceiptCleaner;
import com.wolfmq.broker.domain.transaction.receipt.ReceiptRegistry;
import com.wolfmq.broker.domain.transaction.service.BatchCommitService;
import com.wolfmq.broker.domain.transaction.service.CommitService;
import com.wolfmq.broker.domain.transaction.service.MessageFactory;
import com.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import com.wolfmq.broker.domain.transaction.service.PrepareService;
import com.wolfmq.broker.domain.transaction.service.RollbackService;
import com.wolfmq.broker.domain.transaction.service.SubscribeService;
import com.wolfmq.broker.infra.store.ConsumeOffsetStore;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.domain.config.business.TransactionConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.task.TaskLoader;
import com.wolfmq.domain.domain.transaction.CommitBuffer;

public class TransactionBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public TransactionBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    private TransactionConfig transactionConfig;
    private Transaction transaction;

    private CommitBuffer commitBuffer;
    private BatchCommitService batchCommitService;
    private TransactionMessageService messageService;

    private MessageFactory messageFactory;
    private ReceiptRegistry receiptRegistry;
    private ReceiptCleaner receiptCleaner;

    private CheckerFactory checkerFactory;
    private CheckService checkService;

    @Override
    public void initialize() throws Exception {
        initLibs();
        initTransaction();
        initController();

        initChecker();
    }

    @Override
    public void start() throws Exception {
        receiptCleaner.start();
        batchCommitService.start();

        checkerFactory.start();

        ProducerManager producerManager = context.getBean(ProducerManager.class);
        checkService.inject(producerManager);
        checkService.start();
    }

    @Override
    public void shutdown() throws Exception {
        receiptCleaner.shutdown();
        batchCommitService.shutdown();

        checkerFactory.shutdown();
        checkService.shutdown();
    }

    private void initLibs() {
        brokerConfig = context.getBean(BrokerConfig.class);
        transactionConfig = brokerConfig.getTransactionConfig();

        MQStore mqStore = context.getBean(MQStore.class);
        commitBuffer = new CommitBuffer(transactionConfig);
        messageFactory = new MessageFactory(brokerConfig, commitBuffer);

        batchCommitService = new BatchCommitService(
            transactionConfig, commitBuffer, messageFactory, mqStore
        );

        receiptRegistry = new ReceiptRegistry(transactionConfig);
        receiptCleaner = new ReceiptCleaner(transactionConfig, receiptRegistry);
    }

    private void initTransaction() {
        MQStore mqStore = context.getBean(MQStore.class);
        TopicStore topicStore = context.getBean(TopicStore.class);
        ConsumeOffsetStore consumeOffsetStore = context.getBean(ConsumeOffsetStore.class);

        PrepareService prepareService = new PrepareService(
            transactionConfig, messageFactory, mqStore, receiptRegistry);

        messageService = new TransactionMessageService(
            brokerConfig, commitBuffer, batchCommitService, messageFactory, mqStore, topicStore, consumeOffsetStore);

        SubscribeService subscribeService = new SubscribeService();
        CommitService commitService = new CommitService(messageService, messageFactory);
        RollbackService rollbackService = new RollbackService(messageService);

        transaction = new Transaction(
            receiptRegistry,
            subscribeService,
            prepareService,
            commitService,
            rollbackService
        );
        context.register(transaction);
    }

    private void initController() {
        TransactionController controller = new TransactionController(transaction);
        context.register(controller, ContextNamespace.API);
    }

    private void initChecker() {
        DiscardService discardService = new DiscardService(brokerConfig, messageService);
        checkService = new CheckService(transactionConfig, receiptRegistry);

        TransactionContext transactionContext = TransactionContext.builder()
            .brokerConfig(brokerConfig)
            .commitBuffer(commitBuffer)
            .checkService(checkService)
            .discardService(discardService)
            .messageService(messageService)
            .build();

        checkerFactory = new CheckerFactory(transactionContext);

        TaskLoader loader = context.getBean(TaskLoader.class);
        loader.registerTransactionFactory(checkerFactory);
    }
}
