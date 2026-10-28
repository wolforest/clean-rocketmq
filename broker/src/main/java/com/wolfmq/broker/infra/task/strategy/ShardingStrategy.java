package com.wolfmq.broker.infra.task.strategy;

import com.wolfmq.broker.infra.task.TaskContext;
import com.wolfmq.domain.server.task.TaskStrategy;

public class ShardingStrategy implements TaskStrategy {
    private final TaskContext taskContext;

    public ShardingStrategy(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void load() {

    }
}
