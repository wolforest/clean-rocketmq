package com.wolfmq.store.server.rpc.processor;

import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TopicProcessorTest {

    private ExecutorService executor;
    private TopicProcessor processor;

    @BeforeEach
    void setUp() {
        TopicConfig topicConfig = new TopicConfig();
        TopicStore topicStore = mock(TopicStore.class);
        executor = Executors.newSingleThreadExecutor();
        processor = new TopicProcessor(topicConfig, topicStore, executor);
    }

    @Test
    void testGetCodeSet() {
        assertNotNull(processor.getCodeSet());
        assertTrue(processor.getCodeSet().size() >= 5);
    }

    @Test
    void testGetExecutor() {
        assertSame(executor, processor.getExecutor());
    }

    @Test
    void testRejectReturnsFalse() {
        assertFalse(processor.reject());
    }
}
