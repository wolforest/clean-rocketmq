package cn.coderule.wolfmq.store.domain.consumequeue.queue;

import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
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
        ConsumeQueue[] captured = {null};
        ConsumeQueueRegistry registry = queue -> captured[0] = queue;
        ConsumeQueue queue = mock(ConsumeQueue.class);
        registry.register(queue);
        assertSame(queue, captured[0]);
    }

    @Test
    void testRegisterWithMock() {
        ConsumeQueue queue = mock(ConsumeQueue.class);
        ConsumeQueueRegistry registry = q -> {};
        assertDoesNotThrow(() -> registry.register(queue));
    }

    @Test
    void testRegisterMultiple() {
        ConsumeQueue queue1 = mock(ConsumeQueue.class);
        ConsumeQueue queue2 = mock(ConsumeQueue.class);
        int[] count = {0};
        ConsumeQueueRegistry registry = q -> count[0]++;
        registry.register(queue1);
        registry.register(queue2);
        assertEquals(2, count[0]);
    }
}