package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.transaction.service.MessageFactory;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;

public class TransactionManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private TransactionConfig transactionConfig;
    private Transaction transaction;


    private CommitBuffer commitBuffer;
    private MessageFactory messageFactory;

    private void initLibs() {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        transactionConfig = brokerConfig.getTransactionConfig();

        commitBuffer = new CommitBuffer();
        messageFactory = new MessageFactory(transactionConfig, commitBuffer);
    }

    @Override
    public void initialize() throws Exception {
        initLibs();

        transaction = new Transaction();
        BrokerContext.register(transaction);

        TransactionController controller = new TransactionController(transaction);
        BrokerContext.registerAPI(controller);
    }
    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

}
