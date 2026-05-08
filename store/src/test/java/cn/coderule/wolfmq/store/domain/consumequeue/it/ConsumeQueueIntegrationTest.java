package cn.coderule.wolfmq.store.domain.consumequeue.it;

import cn.coderule.wolfmq.domain.core.enums.store.QueueType;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumeQueueIntegrationTest extends BaseConsumeQueueIntegrationTest {

    private CommitEvent createEvent(long commitOffset, int messageSize, long tagsCode, long queueOffset) {
        MessageBO messageBO = MessageBO.builder()
            .topic(TEST_TOPIC)
            .queueId(TEST_QUEUE_ID)
            .commitOffset(commitOffset)
            .messageLength(messageSize)
            .tagsCode(tagsCode)
            .queueOffset(queueOffset)
            .build();
        messageBO.setSysFlag(0);

        return CommitEvent.of(messageBO, 0);
    }

    @Test
    void testGetOrCreateQueue() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);

        assertNotNull(queue);
        assertEquals(TEST_TOPIC, queue.getTopic());
        assertEquals(TEST_QUEUE_ID, queue.getQueueId());
        assertEquals(QueueType.DEFAULT, queue.getQueueType());
    }

    @Test
    void testGetOrCreateReturnsSameInstance() {
        ConsumeQueue queue1 = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        ConsumeQueue queue2 = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);

        assertSame(queue1, queue2);
    }

    @Test
    void testExistsQueue() {
        assertFalse(consumeQueueFactory.exists(TEST_TOPIC, TEST_QUEUE_ID));

        consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);

        assertTrue(consumeQueueFactory.exists(TEST_TOPIC, TEST_QUEUE_ID));
    }

    @Test
    void testEnqueueAndGet() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        CommitEvent event = createEvent(100L, 256, 12345L, 0);
        queue.enqueue(event);

        QueueUnit unit = queue.get(0);
        assertNotNull(unit);
        assertEquals(100L, unit.getCommitOffset());
        assertEquals(256, unit.getMessageSize());
        assertEquals(12345L, unit.getTagsCode());
    }

    @Test
    void testEnqueueMultipleUnits() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        CommitEvent event0 = createEvent(100L, 128, 100L, 0);
        queue.enqueue(event0);

        CommitEvent event1 = createEvent(228L, 256, 200L, 1);
        queue.enqueue(event1);

        QueueUnit unit0 = queue.get(0);
        assertNotNull(unit0);
        assertEquals(100L, unit0.getCommitOffset());
        assertEquals(128, unit0.getMessageSize());

        QueueUnit unit1 = queue.get(1);
        assertNotNull(unit1);
        assertEquals(228L, unit1.getCommitOffset());
        assertEquals(256, unit1.getMessageSize());
    }

    @Test
    void testGetBatch() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        queue.enqueue(createEvent(100L, 128, 100L, 0));
        queue.enqueue(createEvent(228L, 256, 200L, 1));
        queue.enqueue(createEvent(484L, 512, 300L, 2));

        java.util.List<QueueUnit> units = queue.get(0, 3);
        assertNotNull(units);
        assertEquals(3, units.size());
        assertEquals(100L, units.get(0).getCommitOffset());
        assertEquals(228L, units.get(1).getCommitOffset());
        assertEquals(484L, units.get(2).getCommitOffset());
    }

    @Test
    void testOffsetTracking() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        queue.enqueue(createEvent(100L, 128, 100L, 0));

        assertEquals(0, queue.getMinOffset());
        assertTrue(queue.getMaxOffset() >= 0);
    }

    @Test
    void testManagerEnqueue() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        CommitEvent event = createEvent(500L, 64, 999L, 0);
        consumeQueueManager.enqueue(event);

        QueueUnit unit = consumeQueueManager.get(TEST_TOPIC, TEST_QUEUE_ID, 0);
        assertNotNull(unit);
        assertEquals(500L, unit.getCommitOffset());
        assertEquals(64, unit.getMessageSize());
    }

    @Test
    void testManagerMinMaxOffset() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        consumeQueueManager.enqueue(createEvent(100L, 128, 100L, 0));

        long minOffset = consumeQueueManager.getMinOffset(TEST_TOPIC, TEST_QUEUE_ID);
        assertEquals(0, minOffset);
    }

    @Test
    void testUnitSize() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        assertEquals(UNIT_SIZE, queue.getUnitSize());
    }

    @Test
    void testErrorConsumeQueueForNonExistentTopic() {
        ConsumeQueue result = consumeQueueFactory.getOrCreate("NonExistentTopic", 0);
        assertNotNull(result);
        assertEquals(QueueType.ERROR, result.getQueueType());
    }

    @Test
    void testCommitOffsetUpdate() {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        assertNull(queue.getCommitOffsetByShardId(0));

        CommitEvent event = createEvent(100L, 128, 100L, 0);
        queue.enqueue(event);

        Long commitOffset = queue.getCommitOffsetByShardId(0);
        assertNotNull(commitOffset);
        assertEquals(228L, commitOffset.longValue());
    }

    @Test
    void testFlushAndDestroy() throws Exception {
        ConsumeQueue queue = consumeQueueFactory.getOrCreate(TEST_TOPIC, TEST_QUEUE_ID);
        queue.load();

        queue.enqueue(createEvent(100L, 128, 100L, 0));

        queue.flush(0);
        queue.flush();

        QueueUnit unit = queue.get(0);
        assertNotNull(unit);
        assertEquals(100L, unit.getCommitOffset());

        queue.destroy();
    }
}