package cn.coderule.minimq.domain.domain.cluster.task;

import java.io.Serializable;
import lombok.Data;

@Data
public class StoreTask implements Serializable {
    private String storeGroup;
    private int queueId;

    public StoreTask(String storeGroup, int queueId) {
        this.storeGroup = storeGroup;
        this.queueId = queueId;
    }
}
