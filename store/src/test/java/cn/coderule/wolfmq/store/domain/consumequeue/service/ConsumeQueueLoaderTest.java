package cn.coderule.wolfmq.store.domain.consumequeue.service;

import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueLoaderTest {

    private ConsumeQueueConfig config;
    private ConsumeQueueLoader loader;

    @BeforeEach
    void setUp() {
        config = new ConsumeQueueConfig();
        loader = new ConsumeQueueLoader(config);
    }

    @Test
    void testGetServiceNameNotApplicable() {
        assertNotNull(loader);
    }

    @Test
    void testRegisterAddsQueue() {
        ConsumeQueue queue = mock(ConsumeQueue.class);
        loader.register(queue);

        ConsumeQueue queue2 = mock(ConsumeQueue.class);
        loader.register(queue2);

        verify(queue, never()).getMappedFileQueue();
    }

    @Test
    void testLoadWithEmptyQueueSet() {
        assertDoesNotThrow(() -> loader.load());
    }

    @Test
    void testLoadCallsMappedFileQueueLoadAndCheckSelf() {
        ConsumeQueue queue = mock(ConsumeQueue.class);
        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(queue.getMappedFileQueue()).thenReturn(mfq);

        loader.register(queue);
        loader.load();

        verify(mfq).load();
        verify(mfq).checkSelf();
    }

    @Test
    void testLoadWithMultipleQueues() {
        ConsumeQueue queue1 = mock(ConsumeQueue.class);
        ConsumeQueue queue2 = mock(ConsumeQueue.class);
        MappedFileQueue mfq1 = mock(MappedFileQueue.class);
        MappedFileQueue mfq2 = mock(MappedFileQueue.class);
        when(queue1.getMappedFileQueue()).thenReturn(mfq1);
        when(queue2.getMappedFileQueue()).thenReturn(mfq2);

        loader.register(queue1);
        loader.register(queue2);
        loader.load();

        verify(mfq1).load();
        verify(mfq1).checkSelf();
        verify(mfq2).load();
        verify(mfq2).checkSelf();
    }
}