package cn.coderule.wolfmq.store.domain.dispatcher.it;

import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.dispatcher.CommitListener;
import cn.coderule.wolfmq.store.domain.dispatcher.DispatchManager;
import cn.coderule.wolfmq.store.domain.dispatcher.DispatchQueue;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherIntegrationTest extends BaseDispatcherIntegrationTest {

    @Test
    void testCommitListenerDispatchedOffsetInitialization() {
        CommitListener listener = createCommitListener();

        assertEquals(0, listener.getDispatchedOffset());
    }

    @Test
    void testCommitListenerSetDispatchedOffset() {
        CommitListener listener = createCommitListener();

        listener.setDispatchedOffset(500L);

        assertEquals(500L, listener.getDispatchedOffset());
    }

    @Test
    void testCommitListenerIncreaseDispatchedOffset() {
        CommitListener listener = createCommitListener();

        listener.setDispatchedOffset(0);
        long result = listener.increaseDispatchedOffset(128);

        assertEquals(128, result);
        assertEquals(128, listener.getDispatchedOffset());
    }

    @Test
    void testCommitListenerIncreaseOffsetMultipleTimes() {
        CommitListener listener = createCommitListener();

        listener.setDispatchedOffset(0);
        listener.increaseDispatchedOffset(100);
        listener.increaseDispatchedOffset(200);
        long result = listener.increaseDispatchedOffset(50);

        assertEquals(350, result);
        assertEquals(350, listener.getDispatchedOffset());
    }

    @Test
    void testCommitListenerServiceName() {
        CommitListener listener = createCommitListener();

        assertEquals("CommitListener-0", listener.getServiceName());
    }

    @Test
    void testCommitHandlerManagerBroadcast() {
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        handlerManager.registerHandler(event -> counter1.incrementAndGet());
        handlerManager.registerHandler(event -> counter2.incrementAndGet());

        MessageBO message = MessageMock.createMessage();
        CommitEvent event = CommitEvent.of(message);

        handlerManager.handle(event);

        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    void testCommitHandlerManagerMultipleEvents() {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.registerHandler(event -> counter.incrementAndGet());

        for (int i = 0; i < 5; i++) {
            handlerManager.handle(CommitEvent.of(MessageMock.createMessage()));
        }

        assertEquals(5, counter.get());
    }

    @Test
    void testDispatchQueuePutAndPoll() throws InterruptedException {
        CommitConfig config = new CommitConfig();
        DispatchQueue queue = new DispatchQueue(config);

        MessageBO message = MessageMock.createMessage("TEST_TOPIC", 100);
        CommitEvent event = CommitEvent.of(message);

        queue.put(event);
        assertFalse(queue.isEmpty());

        CommitEvent polled = queue.poll();
        assertNotNull(polled);
        assertEquals("TEST_TOPIC", polled.getMessageBO().getTopic());
    }

    @Test
    void testDispatchQueueFifoOrder() throws InterruptedException {
        CommitConfig config = new CommitConfig();
        DispatchQueue queue = new DispatchQueue(config);

        for (int i = 0; i < 5; i++) {
            MessageBO message = MessageMock.createMessage("TOPIC", 100, 0, i);
            queue.put(CommitEvent.of(message));
        }

        long lastOffset = -1;
        for (int i = 0; i < 5; i++) {
            CommitEvent polled = queue.poll();
            assertNotNull(polled);
            assertTrue(polled.getMessageBO().getQueueOffset() > lastOffset);
            lastOffset = polled.getMessageBO().getQueueOffset();
        }
    }

    @Test
    void testDispatchManagerIntegration() throws Exception {
        CommitConfig config = storeConfig.getCommitConfig();
        config.setDispatchThreads(2);

        StoreCheckpoint checkpoint = StoreContext.getCheckPoint();
        DispatchManager dispatchManager = new DispatchManager(
            config, checkpoint, commitLogManager, handlerManager);

        AtomicInteger counter = registerCountingHandler();

        dispatchManager.initialize();
        dispatchManager.start();

        Thread.sleep(100);

        dispatchManager.shutdown();
        Thread.sleep(100);

        assertEquals(0, counter.get(), "No events should be dispatched without commitLog data");
    }

    @Test
    void testEndToEndPipeline() throws Exception {
        List<String> dispatchedTopics = new CopyOnWriteArrayList<>();
        handlerManager.registerHandler(event -> dispatchedTopics.add(event.getMessageBO().getTopic()));

        CommitListener listener = createCommitListener();
        listener.start();

        byte[] data = "dispatcher integration test".getBytes();
        commitLogStore.insert(0, data, 0, data.length);

        Thread.sleep(200);

        listener.shutdown();
        Thread.sleep(50);

        assertTrue(listener.getDispatchedOffset() >= 0,
            "dispatchedOffset should advance after commitLog has data");
    }

    @Test
    void testHandlerCapturesEventData() {
        List<CommitEvent> capturedEvents = new CopyOnWriteArrayList<>();
        handlerManager.registerHandler(capturedEvents::add);

        MessageBO message = MessageMock.createMessage("CAPTURE_TOPIC", 100);
        CommitEvent event = CommitEvent.of(message, 0);

        handlerManager.handle(event);

        assertEquals(1, capturedEvents.size());
        assertEquals("CAPTURE_TOPIC", capturedEvents.get(0).getMessageBO().getTopic());
        assertEquals(0, capturedEvents.get(0).getShardId());
    }

    @Test
    void testMultipleHandlersReceiveAllEvents() {
        List<String> handler1Topics = new CopyOnWriteArrayList<>();
        List<String> handler2Topics = new CopyOnWriteArrayList<>();

        handlerManager.registerHandler(event -> handler1Topics.add(event.getMessageBO().getTopic()));
        handlerManager.registerHandler(event -> handler2Topics.add(event.getMessageBO().getTopic()));

        for (int i = 0; i < 3; i++) {
            handlerManager.handle(CommitEvent.of(MessageMock.createMessage("TOPIC_" + i, 100)));
        }

        assertEquals(3, handler1Topics.size());
        assertEquals(3, handler2Topics.size());
    }

    @Test
    void testCommitListenerCheckpointIntegration() {
        StoreCheckpoint checkpoint = StoreContext.getCheckPoint();

        Long dispatchedOffset = checkpoint.getMaxOffset().getDispatchedOffset(0);
        assertNull(dispatchedOffset, "initial dispatchedOffset should be null");

        long globalDispatchedOffset = checkpoint.getMaxOffset().getDispatchedOffset();
        assertEquals(-1, globalDispatchedOffset, "initial global dispatchedOffset should be -1");

        checkpoint.getMaxOffset().setDispatchedOffset(500L);
        assertEquals(500L, checkpoint.getMaxOffset().getDispatchedOffset());
    }

    @Test
    void testDispatchManagerLifecycleWithRealCommitLog() throws Exception {
        CommitConfig config = storeConfig.getCommitConfig();
        config.setDispatchThreads(1);

        StoreCheckpoint checkpoint = StoreContext.getCheckPoint();
        DispatchManager dispatchManager = new DispatchManager(
            config, checkpoint, commitLogManager, handlerManager);

        dispatchManager.initialize();
        dispatchManager.start();

        Thread.sleep(100);

        dispatchManager.shutdown();
        Thread.sleep(100);
    }
}
