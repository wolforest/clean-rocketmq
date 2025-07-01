package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionChecker extends ServiceThread {
    private final CheckContext checkContext;
    private final TransactionConfig transactionConfig;
    private final QueueTask task;

    public TransactionChecker(CheckContext checkContext, QueueTask task) {
        this.checkContext = checkContext;
        this.transactionConfig = checkContext.getBrokerConfig().getTransactionConfig();
        this.task = task;
    }

    @Override
    public String getServiceName() {
        return TransactionChecker.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("start transaction checking thread");
        while (!this.isStopped()) {
            long interval = transactionConfig.getCheckInterval();
            this.await(interval);

            check();
        }

        log.info("transaction checking thread end");
    }

    private void check() {
    }
}
