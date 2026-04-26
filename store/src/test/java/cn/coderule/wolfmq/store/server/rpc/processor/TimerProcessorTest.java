package cn.coderule.wolfmq.store.server.rpc.processor;

import cn.coderule.wolfmq.domain.domain.store.api.TimerStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimerProcessorTest {

    private ExecutorService executor;
    private TimerProcessor processor;

    @BeforeEach
    void setUp() {
        TimerStore timerStore = mock(TimerStore.class);
        executor = Executors.newSingleThreadExecutor();
        processor = new TimerProcessor(timerStore, executor);
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
