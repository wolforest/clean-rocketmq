package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptCleaner;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.minimq.broker.domain.transaction.service.BatchCommitService;
import cn.coderule.minimq.broker.domain.transaction.service.CommitService;
import cn.coderule.minimq.broker.domain.transaction.service.MessageFactory;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.broker.domain.transaction.service.PrepareService;
import cn.coderule.minimq.broker.domain.transaction.service.RollbackService;
import cn.coderule.minimq.broker.domain.transaction.service.SubscribeService;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;

public class TransactionManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private TransactionConfig transactionConfig;
    private Transaction transaction;


    private CommitBuffer commitBuffer;
    private BatchCommitService batchCommitService;
    private MessageFactory messageFactory;
    private ReceiptRegistry receiptRegistry;
    private ReceiptCleaner receiptCleaner;

    private void initLibs() {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        transactionConfig = brokerConfig.getTransactionConfig();

        commitBuffer = new CommitBuffer(transactionConfig);
        batchCommitService = new BatchCommitService(commitBuffer);
        messageFactory = new MessageFactory(brokerConfig, commitBuffer);

        receiptRegistry = new ReceiptRegistry(transactionConfig);
        receiptCleaner = new ReceiptCleaner(transactionConfig, receiptRegistry);
    }

    @Override
    public void initialize() throws Exception {
        initLibs();

        MQStore mqStore = BrokerContext.getBean(MQStore.class);
        PrepareService prepareService = new PrepareService(
            transactionConfig, messageFactory, mqStore, receiptRegistry);
        SubscribeService subscribeService = new SubscribeService();

        MessageService messageService = new MessageService(
            brokerConfig,
            commitBuffer,
            batchCommitService,
            messageFactory,
            mqStore
        );
        CommitService commitService = new CommitService(
            messageService, messageFactory
        );

        RollbackService rollbackService = new RollbackService(messageService);

        transaction = new Transaction(
            receiptRegistry,
            subscribeService,
            prepareService,
            commitService,
            rollbackService
        );
        BrokerContext.register(transaction);

        TransactionController controller = new TransactionController(transaction);
        BrokerContext.registerAPI(controller);
    }
    @Override
    public void start() throws Exception {
        receiptCleaner.start();
        batchCommitService.start();
    }

    @Override
    public void shutdown() throws Exception {
        receiptCleaner.shutdown();
        batchCommitService.shutdown();
    }

}
