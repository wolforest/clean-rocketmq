package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.StoreTask;

public class TransactionChecker extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final StoreTask task;

    public TransactionChecker(BrokerConfig brokerConfig, StoreTask task) {
        this.brokerConfig = brokerConfig;
        this.task = task;
    }

    @Override
    public String getServiceName() {
        return TransactionChecker.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
