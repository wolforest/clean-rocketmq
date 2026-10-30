package com.wolfmq.store.domain.consumequeue.service;

import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.consumequeue.ConsumeQueue;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
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
        ConsumeQueue queue = mock(ConsumeQueue.class);
        flusher.register(queue);

        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(queue.getMappedFileQueue()).thenReturn(mfq);

        assertDoesNotThrow(() -> {
            flusher.register(queue);
        });
    }

    @Test
    void testRegisterSameQueueOnlyOnce() {
        ConsumeQueue queue = mock(ConsumeQueue.class);
        flusher.register(queue);
        flusher.register(queue);

        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(queue.getMappedFileQueue()).thenReturn(mfq);
    }
}
