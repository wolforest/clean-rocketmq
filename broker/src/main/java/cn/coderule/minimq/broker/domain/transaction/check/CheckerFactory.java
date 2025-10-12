package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.cluster.task.TaskFactory;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckerFactory implements TaskFactory, Lifecycle {
    private final TransactionContext transactionContext;
    private final ConcurrentMap<Integer, TransactionChecker> checkerMap;

    public CheckerFactory(TransactionContext context) {
        this.checkerMap = new ConcurrentHashMap<>();
        this.transactionContext = context;
    }

    @Override
    public void create(QueueTask task) {
        initStoreGroup(task);

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

    private void initStoreGroup(QueueTask task) {
        CommitBuffer commitBuffer = transactionContext.getCommitBuffer();
        commitBuffer.setStoreGroup(task.getStoreGroup());
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
