package cn.coderule.wolfmq.store.domain.consumequeue.service;

import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueFlusherTest {

    private ConsumeQueueConfig config;
    private StoreCheckpoint checkpoint;
    private ConsumeQueueFlusher flusher;

    @BeforeEach
    void setUp() {
        config = new ConsumeQueueConfig();
        checkpoint = mock(StoreCheckpoint.class);
        flusher = new ConsumeQueueFlusher(config, checkpoint);
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