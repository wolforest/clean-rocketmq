package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.store.StorePath;
import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.domain.test.QueueMock;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultConsumeQueueTest {
    public static int MMAP_FILE_SIZE = 20 * 1000;

    @Test
    void testInsertAndSelect(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());


        queue.destroy();
    }

    @Test
    void testAssignOffset(@TempDir Path tmpDir) {
        ConsumeQueue queue = createConsumeQueue(tmpDir.toString());

        long firstOffset = queue.increaseOffset();
        assertEquals(1, firstOffset);

        long secondOffset = queue.increaseOffset();
        assertEquals(2, secondOffset);

        long thirdOffset = queue.increaseOffset();
        assertEquals(3, thirdOffset);

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

        return CommitEvent.of(messageBO);
    }
}
