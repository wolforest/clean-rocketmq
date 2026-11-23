package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.testing.MessageMock;
import com.wolfmq.store.domain.dispatcher.dispatch.DispatchQueue;
import com.wolfmq.store.domain.dispatcher.dispatch.CommitDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitDispatcherTest {

    private DispatchQueue queue;
    private CommitHandlerManager handlerManager;
    private QueueProgress queueProgress;

    @BeforeEach
    void setUp() {
        queue = new DispatchQueue(new com.wolfmq.common.config.store.CommitConfig());
        handlerManager = new CommitHandlerManager(queueProgress);
        queueProgress = mock(QueueProgress.class);
    }

    @Test
    void testGetServiceName() {
        CommitDispatcher dispatcher = new CommitDispatcher(queue, handlerManager, queueProgress);

        assertEquals("CommitDispatcher", dispatcher.getServiceName());
    }

    @Test
    void testLifecycle() throws Exception {
        CommitDispatcher dispatcher = new CommitDispatcher(queue, handlerManager, queueProgress);

        dispatcher.start();
        Thread.sleep(50);

        assertFalse(dispatcher.isStopped());

        dispatcher.shutdown();
        Thread.sleep(50);

        assertTrue(dispatcher.isStopped());
    }

    @Test
    void testDispatch_EmptyQueue() throws Exception {
        CommitDispatcher dispatcher = new CommitDispatcher(queue, handlerManager, queueProgress);

        dispatcher.start();
        Thread.sleep(100);

        dispatcher.shutdown();
    }

    @Test
    void testDispatch_WithHandler() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.register((CommitEvent _) -> counter.incrementAndGet());

        CommitDispatcher dispatcher = new CommitDispatcher(queue, handlerManager, queueProgress);
        dispatcher.start();

        queue.put(CommitEvent.of(MessageMock.createMessage()));

        Thread.sleep(100);

        assertEquals(1, counter.get());

        dispatcher.shutdown();
    }

    @Test
    void testDispatch_MultipleEvents() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.register((CommitEvent _) -> counter.incrementAndGet());

        CommitDispatcher dispatcher = new CommitDispatcher(queue, handlerManager, queueProgress);
        dispatcher.start();

        for (int i = 0; i < 5; i++) {
            queue.put(CommitEvent.of(MessageMock.createMessage()));
        }

        Thread.sleep(200);

        assertEquals(5, counter.get());

        dispatcher.shutdown();
    }
}
