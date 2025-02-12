package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.model.BrokerContext;

public class TransactionManager implements Lifecycle {
    private Transaction transaction;

    @Override
    public void initialize() {
        transaction = new Transaction();
        transaction.initialize();

        TransactionController controller = new TransactionController(transaction);
        BrokerContext.registerAPI(controller);
    }
    @Override
    public void start() {
        transaction.start();
    }

    @Override
    public void shutdown() {
        transaction.shutdown();
    }

    @Override
    public void cleanup() {
        transaction.cleanup();
    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
