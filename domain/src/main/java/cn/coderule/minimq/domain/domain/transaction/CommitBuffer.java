package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommitBuffer {
    private static final int DEFAULT_QUEUE_LENGTH = 20_000;

    private final TransactionConfig transactionConfig;
    private final ConcurrentMap<Integer, OffsetQueue> offsetMap;
    private final ConcurrentMap<Integer, MessageQueue> operationMap;

    public CommitBuffer(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;

        this.offsetMap = new ConcurrentHashMap<>();
        this.operationMap = new ConcurrentHashMap<>();
    }

    public OffsetQueue getOffsetQueue(int queueId) {
        OffsetQueue offsetQueue = offsetMap.get(queueId);
        if (offsetQueue != null) {
            return offsetQueue;
        }

        offsetQueue = new OffsetQueue(System.currentTimeMillis(), DEFAULT_QUEUE_LENGTH);
        OffsetQueue old = offsetMap.putIfAbsent(queueId, offsetQueue);

        return old != null ? old : offsetQueue;
    }

    public MessageQueue getOperationQueue(int queueId, String storeGroup) {
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
