package cn.coderule.wolfmq.domain.domain.transaction;

import cn.coderule.wolfmq.domain.domain.MessageQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckBufferTest {

    @Test
    void getOrCreateOperationQueue_createsNew() {
        CheckBuffer buffer = new CheckBuffer();
        MessageQueue prepareQueue = MessageQueue.builder()
            .topicName("RMQ_SYS_TRANS_HALF_TOPIC")
            .groupName("myStoreGroup")
            .queueId(0)
            .build();

        MessageQueue operationQueue = buffer.getOrCreateOperationQueue(prepareQueue);
        assertNotNull(operationQueue);
        assertEquals("RMQ_SYS_TRANS_OP_HALF_TOPIC", operationQueue.getTopicName());
        assertEquals("myStoreGroup", operationQueue.getGroupName());
        assertEquals(0, operationQueue.getQueueId());
    }

    @Test
    void getOrCreateOperationQueue_returnsSameForSameKey() {
        CheckBuffer buffer = new CheckBuffer();
        MessageQueue prepareQueue = MessageQueue.builder()
            .topicName("RMQ_SYS_TRANS_HALF_TOPIC")
            .groupName("myStoreGroup")
            .queueId(0)
            .build();

        MessageQueue op1 = buffer.getOrCreateOperationQueue(prepareQueue);
        MessageQueue op2 = buffer.getOrCreateOperationQueue(prepareQueue);
        assertSame(op1, op2);
    }

    @Test
    void getOrCreateOperationQueue_differentQueues() {
        CheckBuffer buffer = new CheckBuffer();
        MessageQueue pq0 = MessageQueue.builder().topicName("half").groupName("group").queueId(0).build();
        MessageQueue pq1 = MessageQueue.builder().topicName("half").groupName("group").queueId(1).build();

        MessageQueue op0 = buffer.getOrCreateOperationQueue(pq0);
        MessageQueue op1 = buffer.getOrCreateOperationQueue(pq1);
        assertNotSame(op0, op1);
        assertEquals(0, op0.getQueueId());
        assertEquals(1, op1.getQueueId());
    }

    @Test
    void getQueue_nonExistent_returnsNull() {
        CheckBuffer buffer = new CheckBuffer();
        MessageQueue pq = MessageQueue.builder().topicName("half").groupName("group").queueId(0).build();
        assertNull(buffer.getQueue(pq));
    }

    @Test
    void getQueue_existing_returnsQueue() {
        CheckBuffer buffer = new CheckBuffer();
        MessageQueue pq = MessageQueue.builder().topicName("half").groupName("group").queueId(0).build();
        buffer.getOrCreateOperationQueue(pq);
        assertNotNull(buffer.getQueue(pq));
    }
}