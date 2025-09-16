package cn.coderule.minimq.broker.infra.task.strategy;

import cn.coderule.minimq.broker.infra.task.TaskContext;
import cn.coderule.minimq.domain.domain.cluster.task.TaskStrategy;

public class BindingStrategy implements TaskStrategy {
    private final TaskContext taskContext;

    public BindingStrategy(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void load() {

    }
}
