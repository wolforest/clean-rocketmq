package cn.coderule.minimq.domain.domain.cluster.task;

import java.io.Serializable;
import lombok.Data;

@Data
public class QueueTask implements Serializable {
    private String storeGroup;
    private int queueId;

    public QueueTask(String storeGroup, int queueId) {
        this.storeGroup = storeGroup;
        this.queueId = queueId;
    }
}
