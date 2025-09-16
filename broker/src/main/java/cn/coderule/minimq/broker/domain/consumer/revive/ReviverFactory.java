package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.cluster.task.TaskFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviverFactory implements TaskFactory, Lifecycle {
    private final ReviveContext context;
    private final RetryService retryService;

    private final ConcurrentMap<Integer, ReviveThread> workerMap;


    public ReviverFactory(ReviveContext context, RetryService retryService) {
        this.context = context;
        this.retryService = retryService;

        this.workerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void create(QueueTask task) {
        workerMap.computeIfAbsent(task.getQueueId(), queueId -> {
            ReviveThread reviver = new ReviveThread(context, queueId, retryService);
            log.info("create reviver: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            startReviver(reviver, task);

            return reviver;
        });
    }

    @Override
    public void destroy(QueueTask task) {
        ReviveThread reviver = workerMap.remove(task.getQueueId());
        if (reviver == null) {
            return;
        }

        try {
            reviver.shutdown();
        } catch (Exception e) {
            log.error("destroy reviver error: storeGroup={}, queueId={}",
                task.getStoreGroup(), task.getQueueId());
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        for (ReviveThread reviver : workerMap.values()) {
            reviver.shutdown();
        }
    }

    private void startReviver(ReviveThread reviver, QueueTask task) {
        try {
            reviver.start();
        } catch (Exception e) {
            log.error("start reviver error: storeGroup={}, queueId={}",
                task.getStoreGroup(), task.getQueueId());
            return;
        }

        log.info("start reviver: storeGroup={}, queueId={}",
            task.getStoreGroup(), task.getQueueId());
    }

}
