package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.producer.ProducerRegister;
import cn.coderule.minimq.broker.domain.transaction.check.service.CheckService;
import cn.coderule.minimq.broker.domain.transaction.check.CheckerFactory;
import cn.coderule.minimq.broker.domain.transaction.check.service.DiscardService;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptCleaner;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.minimq.broker.domain.transaction.service.BatchCommitService;
import cn.coderule.minimq.broker.domain.transaction.service.CommitService;
import cn.coderule.minimq.broker.domain.transaction.service.MessageFactory;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.broker.domain.transaction.service.PrepareService;
import cn.coderule.minimq.broker.domain.transaction.service.RollbackService;
import cn.coderule.minimq.broker.domain.transaction.service.SubscribeService;
import cn.coderule.minimq.broker.infra.store.ConsumeOffsetStore;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.TaskLoader;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;

public class TransactionManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private TransactionConfig transactionConfig;
    private Transaction transaction;

    private CommitBuffer commitBuffer;
    private BatchCommitService batchCommitService;
    private MessageService messageService;

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

        ProducerRegister producerRegister = BrokerContext.getBean(ProducerRegister.class);
        checkService.inject(producerRegister);
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
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        transactionConfig = brokerConfig.getTransactionConfig();

        MQStore mqStore = BrokerContext.getBean(MQStore.class);
        commitBuffer = new CommitBuffer(transactionConfig);
        messageFactory = new MessageFactory(brokerConfig, commitBuffer);

        batchCommitService = new BatchCommitService(
            transactionConfig, commitBuffer, messageFactory, mqStore
        );

        receiptRegistry = new ReceiptRegistry(transactionConfig);
        receiptCleaner = new ReceiptCleaner(transactionConfig, receiptRegistry);
    }

    private void initTransaction() {
        MQStore mqStore = BrokerContext.getBean(MQStore.class);
        TopicStore topicStore = BrokerContext.getBean(TopicStore.class);
        ConsumeOffsetStore consumeOffsetStore = BrokerContext.getBean(ConsumeOffsetStore.class);

        PrepareService prepareService = new PrepareService(
            transactionConfig, messageFactory, mqStore, receiptRegistry);

        messageService = new MessageService(
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
        BrokerContext.register(transaction);
    }

    private void initController() {
        TransactionController controller = new TransactionController(transaction);
        BrokerContext.registerAPI(controller);
    }

    private void initChecker() {
        DiscardService discardService = new DiscardService(brokerConfig, messageService);
        checkService = new CheckService(transactionConfig);

        TransactionContext context = TransactionContext.builder()
            .brokerConfig(brokerConfig)
            .commitBuffer(commitBuffer)
            .checkService(checkService)
            .discardService(discardService)
            .messageService(messageService)
            .build();

        checkerFactory = new CheckerFactory(context);

        TaskLoader loader = BrokerContext.getBean(TaskLoader.class);
        loader.registerTransactionFactory(checkerFactory);
    }
}
