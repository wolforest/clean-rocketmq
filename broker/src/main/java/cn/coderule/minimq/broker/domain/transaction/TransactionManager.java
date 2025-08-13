package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;

public class TransactionManager implements Lifecycle {
    private Transaction transaction;

    @Override
    public void initialize() throws Exception {
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
