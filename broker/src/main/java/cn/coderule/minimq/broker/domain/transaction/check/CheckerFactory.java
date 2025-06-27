package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckerFactory implements TaskFactory, Lifecycle {
    private final BrokerConfig brokerConfig;
    private final ConcurrentMap<Integer, TransactionChecker> checkerMap;

    public CheckerFactory(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.checkerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void create(QueueTask task) {
        checkerMap.computeIfAbsent(task.getQueueId(), queueId -> {
            TransactionChecker checker = new TransactionChecker(brokerConfig, task);
            log.info("create transaction checker: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            checker.start();
            log.info("start transaction checker: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);
            return checker;
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        for (TransactionChecker checker : checkerMap.values()) {
            checker.shutdown();
        }
    }
}
