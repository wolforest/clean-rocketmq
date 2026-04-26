package cn.coderule.wolfmq.store.server.rpc.processor;

import cn.coderule.wolfmq.domain.domain.store.api.meta.SubscriptionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubscriptionProcessorTest {

    private ExecutorService executor;
    private SubscriptionProcessor processor;

    @BeforeEach
    void setUp() {
        SubscriptionStore subscriptionStore = mock(SubscriptionStore.class);
        executor = Executors.newSingleThreadExecutor();
        processor = new SubscriptionProcessor(subscriptionStore, executor);
    }

    @Test
    void testGetCodeSet() {
        assertNotNull(processor.getCodeSet());
        assertEquals(3, processor.getCodeSet().size());
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
