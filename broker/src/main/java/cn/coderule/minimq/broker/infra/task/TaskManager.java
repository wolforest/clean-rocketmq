package cn.coderule.minimq.broker.infra.task;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.server.TaskConfig;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskLoader;

public class TaskManager implements Lifecycle {
    private TaskContext taskContext;
    private TaskLoader taskLoader;

    @Override
    public void initialize() {
        initContext();

        taskLoader = new DefaultTaskLoader(taskContext);
    }



    @Override
    public void start() {
        taskLoader.load();
    }

    @Override
    public void shutdown() {

    }

    private void initContext() {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        TaskConfig taskConfig = brokerConfig.getTaskConfig();
        taskContext = new TaskContext(taskConfig);
    }

}
