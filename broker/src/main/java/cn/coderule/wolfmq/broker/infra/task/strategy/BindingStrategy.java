package cn.coderule.wolfmq.broker.infra.task.strategy;

import cn.coderule.wolfmq.broker.infra.task.TaskContext;
import cn.coderule.wolfmq.domain.server.task.TaskStrategy;

public class BindingStrategy implements TaskStrategy {
    private final TaskContext taskContext;

    public BindingStrategy(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void load() {

    }
}
