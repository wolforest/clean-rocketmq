package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerFactory implements TaskFactory, Lifecycle {
    private final ConcurrentMap<Integer, TimerConsumer> workerMap;

    public TimerFactory() {
        this.workerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void create(QueueTask task) {
        workerMap.computeIfAbsent(task.getQueueId(), queueId -> {
            TimerConsumer consumer = new TimerConsumer(task);
            log.info("create timer consumer: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            consumer.start();
            log.info("start timer consumer: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            return consumer;
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        for (TimerConsumer worker : workerMap.values()) {
            worker.shutdown();
        }
    }
}
