package com.wolfmq.store.domain.consumequeue.error;

import com.wolfmq.store.domain.consumequeue.file.FileConsumeQueue;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueRegistryTest {

    @Test
    void testInterfaceExists() {
        ConsumeQueueRegistry registry = queue -> {};
        assertNotNull(registry);
    }

    @Test
    void testRegisterLambda() {
        FileConsumeQueue[] captured = {null};
        ConsumeQueueRegistry registry = queue -> captured[0] = queue;
        FileConsumeQueue queue = mock(FileConsumeQueue.class);
        registry.register(queue);
        assertSame(queue, captured[0]);
    }

    @Test
    void testRegisterWithMock() {
        FileConsumeQueue queue = mock(FileConsumeQueue.class);
        ConsumeQueueRegistry registry = q -> {};
        assertDoesNotThrow(() -> registry.register(queue));
    }

    @Test
    void testRegisterMultiple() {
        FileConsumeQueue queue1 = mock(FileConsumeQueue.class);
        FileConsumeQueue queue2 = mock(FileConsumeQueue.class);
        int[] count = {0};
        ConsumeQueueRegistry registry = q -> count[0]++;
        registry.register(queue1);
        registry.register(queue2);
        assertEquals(2, count[0]);
    }
}
