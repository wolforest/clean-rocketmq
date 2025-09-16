package cn.coderule.minimq.broker.infra.task.strategy;

import cn.coderule.minimq.broker.infra.task.TaskContext;
import cn.coderule.minimq.domain.domain.cluster.task.TaskStrategy;

public class ShardingStrategy implements TaskStrategy {
    private final TaskContext taskContext;

    public ShardingStrategy(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void load() {

    }
}
