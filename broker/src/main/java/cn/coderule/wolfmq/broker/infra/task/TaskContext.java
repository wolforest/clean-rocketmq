package cn.coderule.wolfmq.broker.infra.task;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.config.business.TaskConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.StoreTask;
import cn.coderule.wolfmq.domain.domain.cluster.task.TaskFactory;
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
