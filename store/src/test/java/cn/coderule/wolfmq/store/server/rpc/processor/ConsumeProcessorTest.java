package cn.coderule.wolfmq.store.server.rpc.processor;

import cn.coderule.wolfmq.domain.domain.store.api.meta.ConsumeOffsetStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeProcessorTest {

    private ExecutorService executor;
    private ConsumeProcessor processor;

    @BeforeEach
    void setUp() {
        ConsumeOffsetStore offsetStore = mock(ConsumeOffsetStore.class);
        executor = Executors.newSingleThreadExecutor();
        processor = new ConsumeProcessor(offsetStore, executor);
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
