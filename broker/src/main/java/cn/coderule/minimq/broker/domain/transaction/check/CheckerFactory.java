package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.cluster.task.TaskFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckerFactory implements TaskFactory, Lifecycle {
    private final BrokerConfig brokerConfig;
    private final TransactionContext transactionContext;
    private final ConcurrentMap<Integer, TransactionChecker> checkerMap;

    public CheckerFactory(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.checkerMap = new ConcurrentHashMap<>();
        this.transactionContext = buildContext();
    }

    private TransactionContext buildContext() {
        return TransactionContext.builder()
            .brokerConfig(brokerConfig)
            .build();
    }

    @Override
    public void create(QueueTask task) {
        checkerMap.computeIfAbsent(task.getQueueId(), queueId -> {
            TransactionChecker checker = new TransactionChecker(transactionContext, task);
            log.info("create transaction checker: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            startChecker(checker, task);
            return checker;
        });
    }

    @Override
    public void destroy(QueueTask task) {
        TransactionChecker checker = checkerMap.remove(task.getQueueId());
        if (checker == null) {
            return;
        }

        try {
            checker.shutdown();
        } catch (Exception e) {
            log.error("shutdown transaction check error: storeGroup={}, queueId={}",
                task.getStoreGroup(), task.getQueueId(), e);
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        for (TransactionChecker checker : checkerMap.values()) {
            checker.shutdown();
        }
    }

    private void startChecker(TransactionChecker checker, QueueTask task) {
        try {
            checker.start();
        } catch (Exception e) {
            log.error("start transaction check error: storeGroup={}, queueId={}",
                task.getStoreGroup(), task.getQueueId(), e);
            return;
        }
        log.info("start transaction checker: storeGroup={}, queueId={}",
            task.getStoreGroup(), task.getQueueId());
    }
}
