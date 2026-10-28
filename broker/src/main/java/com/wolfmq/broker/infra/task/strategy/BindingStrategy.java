package com.wolfmq.broker.infra.task.strategy;

import com.wolfmq.broker.infra.task.TaskContext;
import com.wolfmq.domain.server.task.TaskStrategy;

public class BindingStrategy implements TaskStrategy {
    private final TaskContext taskContext;

    public BindingStrategy(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void load() {

    }
}
