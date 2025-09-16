package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.store.StorePath;
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
    void testRealFile() {
        String dir = "/Users/wingle/mq";
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());

        ConsumeQueue queue = new DefaultConsumeQueue(
            "MQT_8e16d78db3b54a2daa6c9cba381cd6f4",
            0,
            storeConfig.getConsumeQueueConfig(),
            checkpoint
        );

        queue.load();
        List<QueueUnit> units = queue.get(0, 20);
        assertEquals(20, units.size());
    }

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
        assertEquals(event.getMessageBO().getQueueOffset(), last.getQueueOffset());
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

    private ConsumeQueue createConsumeQueue(String dir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        ConsumeQueueConfig queueConfig = storeConfig.getConsumeQueueConfig();
        queueConfig.setFileSize(MMAP_FILE_SIZE);

        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());

        return new DefaultConsumeQueue(
            QueueMock.createTopic(),
            0,
            queueConfig,
            checkpoint
        );
    }

    private CommitEvent createCommitEvent(ConsumeQueue queue) {
        long offset = queue.increaseOffset();
        MessageBO messageBO = MessageMock.createMessage(
            queue.getTopic(),
            queue.getQueueId(),
            offset
        );

        messageBO.setMessageLength(30);
        messageBO.setCommitOffset(50);
        messageBO.setTagsCode(8L);

        return CommitEvent.of(messageBO);
    }
}
