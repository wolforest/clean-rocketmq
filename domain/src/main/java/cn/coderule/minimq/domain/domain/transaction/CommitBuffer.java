package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Setter;

public class CommitBuffer {
    private static final int DEFAULT_QUEUE_LENGTH = 20_000;

    private final TransactionConfig transactionConfig;
    private final ConcurrentMap<Integer, OffsetQueue> offsetMap;
    private final ConcurrentMap<Integer, MessageQueue> operationMap;

    @Setter
    private String storeGroup;

    public CommitBuffer(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;

        this.offsetMap = new ConcurrentHashMap<>();
        this.operationMap = new ConcurrentHashMap<>();
    }

    public Set<Map.Entry<Integer, OffsetQueue>> getOffsetEntrySet() {
        return offsetMap.entrySet();
    }

    public OffsetQueue initOffsetQueue(int queueId) {
        OffsetQueue offsetQueue = offsetMap.get(queueId);
        if (offsetQueue != null) {
            return offsetQueue;
        }

        offsetQueue = new OffsetQueue(System.currentTimeMillis(), DEFAULT_QUEUE_LENGTH);
        OffsetQueue old = offsetMap.putIfAbsent(queueId, offsetQueue);

        return old != null ? old : offsetQueue;
    }

    public MessageQueue getOperationQueue(int queueId) {
        return operationMap.get(queueId);
    }

    public MessageQueue initOperationQueue(int queueId) {
        return initOperationQueue(queueId, this.storeGroup);
    }

    public MessageQueue initOperationQueue(int queueId, String storeGroup) {
        MessageQueue operationQueue = operationMap.get(queueId);
        if (operationQueue != null) {
            return operationQueue;
        }

        operationQueue = createOperationQueue(queueId, storeGroup);
        MessageQueue old = operationMap.putIfAbsent(queueId, operationQueue);

        return old != null ? old : operationQueue;
    }

    private MessageQueue createOperationQueue(Integer queueId, String storeGroup) {
        return MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName(storeGroup)
            .queueId(queueId)
            .build();
    }

}
