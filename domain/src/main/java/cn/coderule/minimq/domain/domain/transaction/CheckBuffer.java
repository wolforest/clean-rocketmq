package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.domain.MessageQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CheckBuffer {
    /**
     * prepareQueue -> operationQueue
     * @rocketmq original name: TransactionalMessageCheckService.opQueueMap
     */
    private final ConcurrentMap<MessageQueue, MessageQueue> queueMap;

    public CheckBuffer() {
        this.queueMap = new ConcurrentHashMap<>();
    }

    public MessageQueue getQueue(MessageQueue prepareQueue) {
        return queueMap.get(prepareQueue);
    }

    /**
     * @rocketmq original name: TransactionalMessageCheckService.getOpQueue
     * @param prepareQueue prepareQueue
     * @return operationQueue
     */
    public MessageQueue getOrCreateOperationQueue(MessageQueue prepareQueue) {
        MessageQueue operationQueue = getQueue(prepareQueue);
        if (operationQueue != null) {
            return operationQueue;
        }

        operationQueue = MessageQueue.builder()
                .topicName(TransactionUtil.buildOperationTopic())
                .groupName(prepareQueue.getGroupName())
                .queueId(prepareQueue.getQueueId())
                .build();

        queueMap.put(prepareQueue, operationQueue);
        return operationQueue;
    }
}
