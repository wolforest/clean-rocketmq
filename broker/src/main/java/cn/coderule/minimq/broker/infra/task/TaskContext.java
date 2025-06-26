package cn.coderule.minimq.broker.infra.task;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.server.TaskConfig;
import cn.coderule.minimq.domain.domain.cluster.task.StoreTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
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
        this.taskConfig = brokerConfig.getTaskConfig();
    }
}
