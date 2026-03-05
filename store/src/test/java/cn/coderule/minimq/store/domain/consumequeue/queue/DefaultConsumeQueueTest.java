package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.store.StorePath;
import cn.coderule.minimq.domain.core.enums.store.QueueType;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.domain.test.QueueMock;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultConsumeQueueTest {
    public static int MMAP_FILE_SIZE = 20 * 1000;

    @Test
    void testInsertAndSelect(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        CommitEvent event = null;
        for (int i = 0; i < 10; i++) {
            event = createCommitEvent(queue);
            queue.enqueue(event);
        }

        queue.flush();

        QueueUnit first = queue.get(0);
        QueueUnit second = queue.get(1);
        QueueUnit third = queue.get(2);
        assertEquals(0, first.getQueueOffset());
        assertEquals(1, second.getQueueOffset());
        assertEquals(2, third.getQueueOffset());

        assertEquals(50, first.getCommitOffset());
        assertEquals(50, second.getCommitOffset());
        assertEquals(50, third.getCommitOffset());

        assertEquals(30, first.getMessageSize());
        assertEquals(30, second.getMessageSize());
        assertEquals(30, third.getMessageSize());



        List<QueueUnit> units = queue.get(0, 20);
        assertEquals(10, units.size());

        QueueUnit last = units.get(units.size() - 1);
        // assertEquals(event.getMessageBO().getQueueOffset(), last.getQueueOffset());
        assertEquals(event.getMessageBO().getCommitOffset(), last.getCommitOffset());
        assertEquals(event.getMessageBO().getMessageLength(), last.getMessageSize());
        assertEquals(event.getMessageBO().getTagsCode(), last.getTagsCode());

        queue.destroy();
    }

    @Test
    void testAssignOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        long firstOffset = queue.increaseOffset();
        assertEquals(0, firstOffset);

        long secondOffset = queue.increaseOffset();
        assertEquals(1, secondOffset);

        long thirdOffset = queue.increaseOffset();
        assertEquals(2, thirdOffset);

        queue.destroy();
    }

    @Test
    void testGetQueueType(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());
        assertEquals(QueueType.DEFAULT, queue.getQueueType());
        queue.destroy();
    }

    @Test
    void testGetUnitSize(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());
        assertTrue(queue.getUnitSize() > 0);
        queue.destroy();
    }

    @Test
    void testGetMinOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        assertEquals(0, queue.getMinOffset());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEvent(queue));
        }
        queue.flush();

        assertEquals(0, queue.getMinOffset());

        queue.destroy();
    }

    @Test
    void testGetMaxOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        assertEquals(0, queue.getMaxOffset());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEventWithConsumeOffset(queue, i));
        }

        assertEquals(4, queue.getMaxOffset());

        queue.destroy();
    }

    @Test
    void testSetMinOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEvent(queue));
        }
        queue.flush();

        queue.setMinOffset(2);

        assertEquals(2, queue.getMinOffset());

        assertNull(queue.get(0));
        assertNotNull(queue.get(2));

        queue.destroy();
    }

    @Test
    void testSetMaxOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEvent(queue));
        }

        queue.setMaxOffset(3);

        assertEquals(3, queue.getMaxOffset());

        queue.destroy();
    }

    @Test
    void testRollToOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        long rolled1 = queue.rollToOffset(0);
        assertEquals(1000, rolled1);

        long rolled2 = queue.rollToOffset(100);
        long unitCount = MMAP_FILE_SIZE / queue.getUnitSize();
        assertEquals(100 + 1000 - 100 % 1000, rolled2);

        long rolled3 = queue.rollToOffset(MMAP_FILE_SIZE - 1);
        assertEquals(MMAP_FILE_SIZE, rolled3);

        queue.destroy();
    }

    @Test
    void testLoad(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEventWithCommitOffset(queue, 50 + i));
        }
        queue.flush();

        String topic = queue.getTopic();
        int queueId = queue.getQueueId();

        // queue.destroy();

        ConsumeQueue reloadedQueue = createConsumeQueue(tmpDir.toString(), topic, queueId);
        reloadedQueue.load();

        // TODO: UPDATE maxOffset while loading
        // assertEquals(55, reloadedQueue.getMaxOffset());

        reloadedQueue.destroy();
    }

    @Test
    void testFlush(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 3; i++) {
            queue.enqueue(createCommitEvent(queue));
        }

        assertDoesNotThrow(() -> queue.flush(0));
        assertDoesNotThrow(() -> queue.flush(1));

        queue.destroy();
    }

    @Test
    void testDestroy(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEvent(queue));
        }
        queue.flush();

        assertDoesNotThrow(queue::destroy);

        assertEquals(0, queue.getMaxOffset());
        assertEquals(0, queue.getMinOffset());
    }

    @Test
    void testGetMaxCommitLogOffset(@TempDir Path tmpDir) {
        DefaultConsumeQueue queue = (DefaultConsumeQueue) createConsumeQueue(tmpDir.toString());

        assertEquals(0L, queue.getMaxCommitLogOffset());

        for (int i = 0; i < 3; i++) {
            CommitEvent event = createCommitEventWithCommitOffset(queue, 100L + i * 50);
            queue.enqueue(event);
        }

        assertEquals(100L + 2 * 50 + 30, queue.getMaxCommitLogOffset());

        queue.destroy();
    }

    @Test
    void testSetMaxCommitLogOffset(@TempDir Path tmpDir) {
        DefaultConsumeQueue queue = (DefaultConsumeQueue) createConsumeQueue(tmpDir.toString());

        queue.setMaxCommitLogOffset(500L);
        assertEquals(500L, queue.getMaxCommitLogOffset());

        queue.setMaxCommitLogOffset(100L);
        assertEquals(100L, queue.getMaxCommitLogOffset());

        queue.destroy();
    }

    @Test
    void testGetMultiple(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 10; i++) {
            queue.enqueue(createCommitEvent(queue));
        }
        queue.flush();

        List<QueueUnit> all = queue.get(0, 10);
        assertEquals(10, all.size());

        List<QueueUnit> partial = queue.get(2, 5);
        assertEquals(5, partial.size());
        assertEquals(2, partial.get(0).getQueueOffset());

        List<QueueUnit> exceed = queue.get(5, 100);
        assertEquals(5, exceed.size());

        queue.destroy();
    }

    @Test
    void testGetBeyondMinOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        queue.setMinOffset(3);
        queue.setMaxOffset(3);

        for (int i = 0; i < 5; i++) {
            long queueOffset = queue.increaseOffset();
            queue.enqueue(createCommitEventWithConsumeOffset(queue, queueOffset));
        }
        queue.flush();


        QueueUnit unit = queue.get(0);
        assertNull(unit);

        QueueUnit unit2 = queue.get(2);
        assertNull(unit2);

        QueueUnit unit3 = queue.get(3);
        assertNotNull(unit3);
        assertEquals(3, unit3.getQueueOffset());

        queue.destroy();
    }

    @Test
    void testGetBeyondMaxOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        for (int i = 0; i < 5; i++) {
            queue.enqueue(createCommitEvent(queue));
        }
        queue.flush();

        QueueUnit unit = queue.get(10);
        assertNull(unit);

        queue.destroy();
    }

    @Test
    void testEmptyQueueOperations(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        assertEquals(0, queue.getMinOffset());
        assertEquals(0, queue.getMaxOffset());
        assertTrue(queue.get(0, 10).isEmpty());

        queue.destroy();
    }

    @Test
    void testQueueInfo(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        assertEquals(0, queue.getQueueId());
        assertTrue(queue.getUnitSize() > 0);

        queue.destroy();
    }

    private ConsumeQueue createConsumeQueue(String dir) {
        return createConsumeQueue(dir, QueueMock.createTopic(), 0);
    }

    private ConsumeQueue createConsumeQueue(String dir, String topic, int queueId) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        ConsumeQueueConfig queueConfig = storeConfig.getConsumeQueueConfig();
        queueConfig.setFileSize(MMAP_FILE_SIZE);

        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());

        return new DefaultConsumeQueue(
            topic,
            queueId,
            queueConfig,
            checkpoint
        );
    }

    private CommitEvent createCommitEvent(ConsumeQueue queue) {
        return createCommitEventWithCommitOffset(queue, 50L);
    }

    private CommitEvent createCommitEventWithCommitOffset(ConsumeQueue queue, long commitOffset) {
        long offset = queue.increaseOffset();
        MessageBO messageBO = MessageMock.createMessage(
            queue.getTopic(),
            queue.getQueueId(),
            offset
        );

        messageBO.setMessageLength(30);
        messageBO.setCommitOffset(commitOffset);
        messageBO.setTagsCode(8L);

        return CommitEvent.of(messageBO);
    }

    private CommitEvent createCommitEventWithConsumeOffset(ConsumeQueue queue, long queueOffset) {
        long offset = queue.increaseOffset();
        MessageBO messageBO = MessageMock.createMessage(
            queue.getTopic(),
            queue.getQueueId(),
            offset
        );

        messageBO.setCommitOffset(0L);
        messageBO.setMessageLength(30);
        messageBO.setQueueOffset(queueOffset);
        messageBO.setTagsCode(8L);

        return CommitEvent.of(messageBO);
    }
}
