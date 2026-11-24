package com.wolfmq.store.domain.consumequeue.service;

import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.file.FileConsumeQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueFlusherTest {

    private ConsumeQueueConfig config;
    private ConsumeQueueFlusher flusher;

    @BeforeEach
    void setUp() {
        config = new ConsumeQueueConfig();
        flusher = new ConsumeQueueFlusher(config);
    }

    @Test
    void testGetServiceName() {
        assertEquals("ConsumeQueueFlusher", flusher.getServiceName());
    }

    @Test
    void testRegisterAddsQueue() {
        FileConsumeQueue queue = mock(FileConsumeQueue.class);
        assertDoesNotThrow(() -> flusher.register(queue));
    }

    @Test
    void testRegisterSameQueueOnlyOnce() {
        FileConsumeQueue queue = mock(FileConsumeQueue.class);
        assertDoesNotThrow(() -> {
            flusher.register(queue);
            flusher.register(queue);
        });
    }
}
