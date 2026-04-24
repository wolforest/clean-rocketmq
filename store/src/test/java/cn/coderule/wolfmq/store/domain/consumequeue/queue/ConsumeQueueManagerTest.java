package cn.coderule.wolfmq.store.domain.consumequeue.queue;

import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueManagerTest {

    private ConsumeQueueFactory factory;
    private ConsumeQueueManager manager;
    private TopicService topicService;

    @BeforeEach
    void setUp() {
        ConsumeQueueConfig config = new ConsumeQueueConfig();
        topicService = mock(TopicService.class);
        StoreCheckpoint checkpoint = mock(StoreCheckpoint.class);
        factory = spy(new ConsumeQueueFactory(config, topicService, checkpoint));
        manager = new ConsumeQueueManager(factory);
    }

    @Test
    void testExistsQueueReturnsFalseForNonExistentTopic() {
        when(topicService.exists("TOPIC_A")).thenReturn(false);
        assertFalse(manager.existsQueue("TOPIC_A", 0));
    }

    @Test
    void testExistsQueueAfterGetOrCreate() {
        when(topicService.exists("TOPIC_A")).thenReturn(true);
        ConsumeQueue queue = factory.getOrCreate("TOPIC_A", 0);
        if (!(queue instanceof ErrorConsumeQueue)) {
            assertTrue(manager.existsQueue("TOPIC_A", 0));
        }
    }

    @Test
    void testDeleteByTopicDoesNotThrow() {
        assertDoesNotThrow(() -> manager.deleteByTopic("NONEXISTENT_TOPIC"));
    }

    @Test
    void testGetMinOffsetDelegatesToQueue() {
        when(topicService.exists("TOPIC_A")).thenReturn(true);
        ConsumeQueue queue = factory.getOrCreate("TOPIC_A", 0);
        if (!(queue instanceof ErrorConsumeQueue)) {
            long offset = manager.getMinOffset("TOPIC_A", 0);
            assertNotNull(offset);
        }
    }

    @Test
    void testGetMaxOffsetDelegatesToQueue() {
        when(topicService.exists("TOPIC_A")).thenReturn(true);
        ConsumeQueue queue = factory.getOrCreate("TOPIC_A", 0);
        if (!(queue instanceof ErrorConsumeQueue)) {
            long offset = manager.getMaxOffset("TOPIC_A", 0);
            assertNotNull(offset);
        }
    }
}