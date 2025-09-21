package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.domain.MessageQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CheckBuffer {
    // prepareQueue -> commitQueue
    private final ConcurrentMap<MessageQueue, MessageQueue> queueMap;

    public CheckBuffer() {
        this.queueMap = new ConcurrentHashMap<>();
    }

    public MessageQueue getCommitQueue(MessageQueue prepareQueue) {
        return queueMap.get(prepareQueue);
    }

    public MessageQueue createCommitQueue(MessageQueue prepareQueue) {
        MessageQueue commitQueue = MessageQueue.builder()
                .topicName(TransactionUtil.buildOperationTopic())
                .groupName(prepareQueue.getGroupName())
                .queueId(prepareQueue.getQueueId())
                .build();

        queueMap.put(prepareQueue, commitQueue);
        return commitQueue;
    }
}
