package cn.coderule.minimq.broker.infra.task.strategy;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.infra.task.TaskContext;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.cluster.task.StoreTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskLoader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class EmbedTaskLoader implements TaskLoader {
    private final TaskContext taskContext;

    public EmbedTaskLoader(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void setTimerFactory(TaskFactory factory) {
    }

    @Override
    public void setReviveFactory(TaskFactory factory) {
    }

    @Override
    public void setTransactionFactory(TaskFactory factory) {
    }

    @Override
    public void load() {
        initStoreTask();
        startStoreTask();
    }

    private void startStoreTask() {
        startTimerTask();
        startReviveTask();
        startTransactionTask();
    }

    private void startTransactionTask() {
        if (null == taskContext.getTransactionFactory()) {
            return;
        }

        StoreTask task = taskContext.getTask();
        if (CollectionUtil.isEmpty(task.getTransactionQueueSet())) {
            return;
        }

        for (Integer queueId : task.getTransactionQueueSet()) {
            QueueTask queueTask = new QueueTask(task.getStoreGroup(), queueId);
            TaskFactory factory = taskContext.getTransactionFactory();
            factory.create(queueTask);
        }
    }

    private void startReviveTask() {
        if (null == taskContext.getReviveFactory()) {
            return;
        }

        StoreTask task = taskContext.getTask();
        if (CollectionUtil.isEmpty(task.getReviveQueueSet())) {
            return;
        }

        for (Integer queueId : task.getReviveQueueSet()) {
            QueueTask queueTask = new QueueTask(task.getStoreGroup(), queueId);
            TaskFactory factory = taskContext.getReviveFactory();
            factory.create(queueTask);
        }
    }

    private void startTimerTask() {
        if (null == taskContext.getTimerFactory()) {
            return;
        }

        StoreTask task = taskContext.getTask();
        if (CollectionUtil.isEmpty(task.getTimerQueueSet())) {
            return;
        }

        for (Integer queueId : task.getTimerQueueSet()) {
            QueueTask queueTask = new QueueTask(task.getStoreGroup(), queueId);
            TaskFactory factory = taskContext.getTimerFactory();
            factory.create(queueTask);
        }
    }

    private void initStoreTask() {
        StoreTask task = new StoreTask();
        taskContext.setTask(task);

        BrokerConfig brokerConfig = taskContext.getBrokerConfig();
        task.setStoreGroup(brokerConfig.getGroup());

        initTimerTask(task);
        initReviveTask(task);
        initTransactionTask(task);
    }

    private void initReviveTask(StoreTask task) {
        TopicConfig topicConfig = taskContext.getBrokerConfig().getTopicConfig();
        List<Integer> reviveQueueList = IntStream
            .range(0, topicConfig.getReviveQueueNum())
            .boxed()
            .toList();
        Set<Integer> reviveQueueSet = new TreeSet<>(reviveQueueList);
        task.setReviveQueueSet(reviveQueueSet);
    }

    private void initTimerTask(StoreTask task) {
        TopicConfig topicConfig = taskContext.getBrokerConfig().getTopicConfig();
        List<Integer> timerQueueList = IntStream
            .range(0, topicConfig.getTimerQueueNum())
            .boxed()
            .toList();
        Set<Integer> timerQueueSet = new TreeSet<>(timerQueueList);
        task.setTimerQueueSet(timerQueueSet);
    }

    private void initTransactionTask(StoreTask task) {
        TopicConfig topicConfig = taskContext.getBrokerConfig().getTopicConfig();
        List<Integer> transactionQueueList = IntStream
            .range(0, topicConfig.getTransactionQueueNum())
            .boxed()
            .toList();
        Set<Integer> transactionQueueSet = new TreeSet<>(transactionQueueList);
        task.setTransactionQueueSet(transactionQueueSet);
    }

}
