package cn.coderule.minimq.broker.infra.task;

import cn.coderule.minimq.domain.config.server.TaskConfig;
import cn.coderule.minimq.domain.domain.cluster.task.StoreTaskSet;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;
import java.io.Serializable;
import lombok.Data;

@Data
public class TaskContext implements Serializable {
    private final TaskConfig taskConfig;

    private TaskFactory timerFactory;
    private TaskFactory reviveFactory;
    private TaskFactory transactionFactory;

    private StoreTaskSet task;

    public TaskContext(TaskConfig taskConfig) {
        this.taskConfig = taskConfig;
    }
}
