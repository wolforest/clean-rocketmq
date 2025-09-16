package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.domain.timer.transit.TimerQueueConsumer;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.cluster.task.TaskFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerFactory implements TaskFactory, Lifecycle {
    private final TimerContext context;
    private final ConcurrentMap<Integer, TimerQueueConsumer> workerMap;

    public TimerFactory(TimerContext context) {
        this.context = context;
        this.workerMap = new ConcurrentHashMap<>();
    }

    @Override
    public void create(QueueTask task) {
        context.initQueueTask(task);

        workerMap.computeIfAbsent(task.getQueueId(), queueId -> {
            TimerQueueConsumer consumer = new TimerQueueConsumer(context, task);
            log.info("create timer consumer: storeGroup={}, queueId={}",
                task.getStoreGroup(), queueId);

            startTimer(consumer, task);
            return consumer;
        });
    }

    @Override
    public void destroy(QueueTask task) {
        TimerQueueConsumer consumer = workerMap.remove(task.getQueueId());
        if (consumer == null) {
            return;
        }

        try {
            consumer.shutdown();
        } catch (Exception e) {
            log.error("destroy timer consumer error: storeGroup={}, queueId={}",
                task.getStoreGroup(), task.getQueueId(), e);
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        for (TimerQueueConsumer consumer : workerMap.values()) {
            consumer.shutdown();
        }
    }

    private void startTimer(TimerQueueConsumer consumer, QueueTask task) {
        try {
            consumer.start();
        } catch (Exception e) {
            log.error("start timer consumer error: storeGroup={}, queueId={}",
                task.getStoreGroup(), task.getQueueId(), e);
            return;
        }
        log.info("start timer consumer: storeGroup={}, queueId={}",
            task.getStoreGroup(), task.getQueueId());
    }
}
