package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.test.MessageMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

    private DispatchQueue queue;
    private CommitHandlerManager handlerManager;

    @BeforeEach
    void setUp() {
        queue = new DispatchQueue(new cn.coderule.minimq.domain.config.store.CommitConfig());
        handlerManager = new CommitHandlerManager();
    }

    @Test
    void testGetServiceName() {
        Dispatcher dispatcher = new Dispatcher(queue, handlerManager);

        assertEquals("Dispatcher", dispatcher.getServiceName());
    }

    @Test
    void testLifecycle() throws Exception {
        Dispatcher dispatcher = new Dispatcher(queue, handlerManager);

        dispatcher.start();
        Thread.sleep(50);

        assertFalse(dispatcher.isStopped());

        dispatcher.shutdown();
        Thread.sleep(50);

        assertTrue(dispatcher.isStopped());
    }

    @Test
    void testDispatch_EmptyQueue() throws Exception {
        Dispatcher dispatcher = new Dispatcher(queue, handlerManager);

        dispatcher.start();
        Thread.sleep(100);

        dispatcher.shutdown();
    }

    @Test
    void testDispatch_WithHandler() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.registerHandler((CommitEvent event) -> counter.incrementAndGet());

        Dispatcher dispatcher = new Dispatcher(queue, handlerManager);
        dispatcher.start();

        queue.put(CommitEvent.of(MessageMock.createMessage()));

        Thread.sleep(100);

        assertEquals(1, counter.get());

        dispatcher.shutdown();
    }

    @Test
    void testDispatch_MultipleEvents() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.registerHandler((CommitEvent event) -> counter.incrementAndGet());

        Dispatcher dispatcher = new Dispatcher(queue, handlerManager);
        dispatcher.start();

        for (int i = 0; i < 5; i++) {
            queue.put(CommitEvent.of(MessageMock.createMessage()));
        }

        Thread.sleep(200);

        assertEquals(5, counter.get());

        dispatcher.shutdown();
    }
}
