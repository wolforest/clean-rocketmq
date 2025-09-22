package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class BatchCommitContext implements Serializable {
    private long startTime;
    private long firstTime;
    private Map<Integer, MessageBO> sendMap;
    private boolean overflow;

    public BatchCommitContext() {
        this.startTime = System.currentTimeMillis();
        this.firstTime = startTime;
        this.sendMap = new HashMap<>();
        this.overflow = false;
    }

    public void add(MessageBO messageBO) {
        this.sendMap.put(messageBO.getQueueId(), messageBO);
    }

    public void updateFirstTime(long newTime) {
        this.firstTime = Math.min(this.firstTime, newTime);
    }
}
