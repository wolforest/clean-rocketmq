package cn.coderule.minimq.broker.infra.task.strategy;

import cn.coderule.minimq.broker.infra.task.TaskContext;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskLoader;

public class ShardingTaskLoader implements TaskLoader {
    private final TaskContext taskContext;

    public ShardingTaskLoader(TaskContext taskContext) {
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

    }
}
