package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitHandler;
import cn.coderule.minimq.domain.test.MessageMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CommitHandlerManagerTest {

    private CommitHandlerManager manager;

    @BeforeEach
    void setUp() {
        manager = new CommitHandlerManager();
    }

    @Test
    void testRegisterHandler() {
        assertTrue(manager.isEmpty());

        CommitHandler handler = new MockCommitHandler();
        manager.registerHandler(handler);

        assertFalse(manager.isEmpty());
    }

    @Test
    void testIsEmpty_NoHandlers() {
        assertTrue(manager.isEmpty());
    }

    @Test
    void testIsEmpty_WithHandlers() {
        manager.registerHandler(new MockCommitHandler());
        assertFalse(manager.isEmpty());
    }

    @Test
    void testHandle_BroadcastsToAll() {
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        manager.registerHandler((CommitEvent event) -> counter1.incrementAndGet());
        manager.registerHandler((CommitEvent event) -> counter2.incrementAndGet());

        manager.handle(CommitEvent.of(MessageMock.createMessage()));

        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    void testHandle_EmptyList() {
        assertTrue(manager.isEmpty());

        assertDoesNotThrow(() -> manager.handle(CommitEvent.of(MessageMock.createMessage())));
    }

    @Test
    void testHandle_MultipleEvents() {
        AtomicInteger counter = new AtomicInteger(0);
        manager.registerHandler((CommitEvent event) -> counter.incrementAndGet());

        for (int i = 0; i < 5; i++) {
            manager.handle(CommitEvent.of(MessageMock.createMessage()));
        }

        assertEquals(5, counter.get());
    }

    @Test
    void testHandle_MultipleHandlers() {
        AtomicInteger sum = new AtomicInteger(0);
        manager.registerHandler((CommitEvent event) -> sum.addAndGet(1));
        manager.registerHandler((CommitEvent event) -> sum.addAndGet(10));
        manager.registerHandler((CommitEvent event) -> sum.addAndGet(100));

        manager.handle(CommitEvent.of(MessageMock.createMessage()));

        assertEquals(111, sum.get());
    }

    @Test
    void testMultipleRegistrations() {
        AtomicInteger counter = new AtomicInteger(0);
        CommitHandler handler = event -> counter.incrementAndGet();

        manager.registerHandler(handler);
        manager.registerHandler(handler);
        manager.registerHandler(handler);

        manager.handle(CommitEvent.of(MessageMock.createMessage()));

        assertEquals(3, counter.get());
    }

    private static class MockCommitHandler implements CommitHandler {
        @Override
        public void handle(CommitEvent event) {
        }
    }
}
