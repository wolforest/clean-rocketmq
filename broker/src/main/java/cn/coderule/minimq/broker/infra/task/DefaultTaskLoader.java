package cn.coderule.minimq.broker.infra.task;

import cn.coderule.minimq.broker.infra.task.strategy.BindingStrategy;
import cn.coderule.minimq.broker.infra.task.strategy.EmbedStrategy;
import cn.coderule.minimq.broker.infra.task.strategy.ShardingStrategy;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.business.TaskConfig;
import cn.coderule.minimq.domain.domain.cluster.task.TaskFactory;
import cn.coderule.minimq.domain.domain.cluster.task.TaskLoader;
import cn.coderule.minimq.domain.domain.cluster.task.TaskStrategy;

public class DefaultTaskLoader implements TaskLoader {
    private final TaskContext taskContext;

    public DefaultTaskLoader(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void setTimerFactory(TaskFactory factory) {
        taskContext.setTimerFactory(factory);
    }

    @Override
    public void setReviveFactory(TaskFactory factory) {
        taskContext.setReviveFactory(factory);
    }

    @Override
    public void setTransactionFactory(TaskFactory factory) {
        taskContext.setTransactionFactory(factory);
    }

    @Override
    public void load() {
        TaskStrategy strategy = getStrategy();
        if (strategy != null) {
            strategy.load();
        }
    }

    private TaskStrategy getStrategy() {
        TaskStrategy strategy = null;

        BrokerConfig brokerConfig = taskContext.getBrokerConfig();
        if (brokerConfig.isEnableEmbedStore()) {
            strategy = new EmbedStrategy(taskContext);
        }

        TaskConfig taskConfig = brokerConfig.getTaskConfig();
        if ("binding".equalsIgnoreCase(taskConfig.getTaskMode())) {
            strategy = new BindingStrategy(taskContext);
        }

        if ("sharding".equalsIgnoreCase(taskConfig.getTaskMode())) {
            strategy = new ShardingStrategy(taskContext);
        }

        return strategy;
    }
}
