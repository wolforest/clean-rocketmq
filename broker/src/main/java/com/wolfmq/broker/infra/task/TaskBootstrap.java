package com.wolfmq.broker.infra.task;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.server.bootstrap.BrokerContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.task.TaskLoader;

public class TaskBootstrap implements Lifecycle {
    private TaskContext taskContext;
    private TaskLoader taskLoader;

    @Override
    public void initialize() throws Exception {
        initContext();

        taskLoader = new DefaultTaskLoader(taskContext);
        BrokerContext.register(taskLoader, TaskLoader.class);
    }

    @Override
    public void start() throws Exception {
        taskLoader.load();
    }

    @Override
    public void shutdown() throws Exception {
    }

    private void initContext() {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        taskContext = new TaskContext(brokerConfig);
    }

}
