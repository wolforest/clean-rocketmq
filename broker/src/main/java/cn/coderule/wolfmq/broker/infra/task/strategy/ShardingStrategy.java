package cn.coderule.wolfmq.broker.infra.task.strategy;

import cn.coderule.wolfmq.broker.infra.task.TaskContext;
import cn.coderule.wolfmq.domain.domain.cluster.task.TaskStrategy;

public class ShardingStrategy implements TaskStrategy {
    private final TaskContext taskContext;

    public ShardingStrategy(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void load() {

    }
}
