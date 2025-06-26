package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.cluster.task.StoreTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CheckManager implements TaskFactory, Lifecycle {
    private final BrokerConfig brokerConfig;
    private final ConcurrentMap<Integer, TransactionChecker> checkerMap;

    public CheckManager(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.checkerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void create(StoreTask taskSet) {
        Set<Integer> queueSet = taskSet.getTransactionQueueSet();
        if (CollectionUtil.isEmpty(queueSet)) {
            return;
        }

        for (Integer queueId : queueSet) {
            checkerMap.computeIfAbsent(queueId, k -> {
                QueueTask task = new QueueTask(taskSet.getStoreGroup(), queueId);
                TransactionChecker checker = new TransactionChecker(brokerConfig, task);

                checker.start();
                return checker;
            });
        }
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
