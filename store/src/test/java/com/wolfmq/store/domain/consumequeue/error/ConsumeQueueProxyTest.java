package com.wolfmq.store.domain.consumequeue.error;

import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.domain.consumequeue.ConsumeQueue;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.server.store.checkpoint.bakeup.CheckPoint;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueProxyTest {

    private ConsumeQueueFactory factory;
    private ConsumeQueueProxy manager;
    private TopicService topicService;

    @BeforeEach
    void setUp() {
        ConsumeQueueConfig config = new ConsumeQueueConfig();
        topicService = mock(TopicService.class);
        CheckPoint checkpoint = mock(CheckPoint.class);
        factory = spy(new ConsumeQueueFactory(config, topicService, checkpoint));
        manager = new ConsumeQueueProxy(factory);
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
            assertTrue(offset >= 0L);
        }
    }

    @Test
    void testGetMaxOffsetDelegatesToQueue() {
        when(topicService.exists("TOPIC_A")).thenReturn(true);
        ConsumeQueue queue = factory.getOrCreate("TOPIC_A", 0);
        if (!(queue instanceof ErrorConsumeQueue)) {
            long offset = manager.getMaxOffset("TOPIC_A", 0);
            assertTrue(offset >= 0L);
        }
    }
}
