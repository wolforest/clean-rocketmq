package com.wolfmq.broker.infra.task;

import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.config.business.TaskConfig;
import com.wolfmq.common.server.task.StoreTask;
import com.wolfmq.common.server.task.TaskFactory;
import java.io.Serializable;
import lombok.Data;

@Data
public class TaskContext implements Serializable {
    private final BrokerConfig brokerConfig;
    private final TaskConfig taskConfig;

    private TaskFactory timerFactory;
    private TaskFactory reviveFactory;
    private TaskFactory transactionFactory;

    private StoreTask task;

    public TaskContext(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.taskConfig = brokerConfig.getTask();
    }
}
