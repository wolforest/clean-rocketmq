package cn.coderule.minimq.broker.infra.task;

import cn.coderule.minimq.broker.infra.task.strategy.BindingTaskLoader;
import cn.coderule.minimq.broker.infra.task.strategy.EmbedTaskLoader;
import cn.coderule.minimq.broker.infra.task.strategy.ShardingTaskLoader;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.business.TaskConfig;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskLoader;

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
        TaskLoader loader = null;

        BrokerConfig brokerConfig = taskContext.getBrokerConfig();
        if (brokerConfig.isEnableEmbedStore()) {
            loader = new EmbedTaskLoader(taskContext);
        }

        TaskConfig taskConfig = brokerConfig.getTaskConfig();
        if ("binding".equalsIgnoreCase(taskConfig.getTaskMode())) {
            loader = new BindingTaskLoader(taskContext);
        }

        if ("sharding".equalsIgnoreCase(taskConfig.getTaskMode())) {
            loader = new ShardingTaskLoader(taskContext);
        }

        if (loader != null) {
            loader.load();
        }
    }
}
