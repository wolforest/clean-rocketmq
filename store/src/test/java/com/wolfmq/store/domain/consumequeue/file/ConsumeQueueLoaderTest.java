package com.wolfmq.store.domain.consumequeue.file;

import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.domain.consumequeue.ConsumeQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testConstructor() {
        assertNotNull(loader);
    }

    @Test
    void testRegisterIgnoresNonFileQueue() {
        ConsumeQueue queue = mock(ConsumeQueue.class);
        loader.register(queue);
        loader.load();

        verify(queue, never()).getMappedFileQueue();
    }

    @Test
    void testLoadWithEmptyQueueSet() {
        assertDoesNotThrow(() -> loader.load());
    }

    @Test
    void testLoadCallsMappedFileQueueLoadAndCheckSelf() {
        FileConsumeQueue queue = mock(FileConsumeQueue.class);
        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(queue.getMappedFileQueue()).thenReturn(mfq);

        loader.register(queue);
        loader.load();

        verify(mfq).load();
        verify(mfq).checkSelf();
    }

    @Test
    void testLoadWithMultipleQueues() {
        FileConsumeQueue queue1 = mock(FileConsumeQueue.class);
        FileConsumeQueue queue2 = mock(FileConsumeQueue.class);
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
