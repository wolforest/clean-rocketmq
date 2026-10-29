package com.wolfmq.broker.infra.task;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.task.TaskLoader;

public class TaskBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public TaskBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private TaskContext taskContext;
    private TaskLoader taskLoader;

    @Override
    public void initialize() throws Exception {
        initContext();

        taskLoader = new DefaultTaskLoader(taskContext);
        context.register(taskLoader, TaskLoader.class, ContextNamespace.APP);
    }

    @Override
    public void start() throws Exception {
        taskLoader.load();
    }

    @Override
    public void shutdown() throws Exception {
    }

    private void initContext() {
        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
        taskContext = new TaskContext(brokerConfig);
    }

}
