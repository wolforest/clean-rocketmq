package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import cn.coderule.minimq.store.domain.mq.revive.RetryService;
import cn.coderule.minimq.store.domain.mq.revive.ReviveContext;
import cn.coderule.minimq.store.domain.mq.revive.ReviveThread;
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

            reviver.start();
            log.info("start reviver: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            return reviver;
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        for (ReviveThread reviver : workerMap.values()) {
            reviver.shutdown();
        }
    }


}
