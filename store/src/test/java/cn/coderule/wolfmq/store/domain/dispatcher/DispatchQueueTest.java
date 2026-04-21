package cn.coderule.wolfmq.store.domain.dispatcher;

import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispatchQueueTest {

    private DispatchQueue queue;

    @BeforeEach
    void setUp() {
        CommitConfig config = new CommitConfig();
        queue = new DispatchQueue(config);
    }

    @Test
    void testPutAndPoll() throws InterruptedException {
        MessageBO message = MessageMock.createMessage("TEST", 100);
        CommitEvent event = CommitEvent.of(message);

        queue.put(event);
        assertFalse(queue.isEmpty());

        CommitEvent polled = queue.poll();
        assertNotNull(polled);
        assertEquals(message.getTopic(), polled.getMessageBO().getTopic());
    }

    @Test
    void testOfferAndPoll() throws InterruptedException {
        MessageBO message = MessageMock.createMessage("TEST", 100);
        CommitEvent event = CommitEvent.of(message);

        boolean offered = queue.offer(event);
        assertTrue(offered);

        CommitEvent polled = queue.poll();
        assertNotNull(polled);
    }

    @Test
    void testOfferWithTimeout() throws InterruptedException {
        MessageBO message = MessageMock.createMessage("TEST", 100);
        CommitEvent event = CommitEvent.of(message);

        boolean offered = queue.offer(event, 1000);
        assertTrue(offered);
    }

    @Test
    void testPollWithTimeout_NoData() throws InterruptedException {
        CommitEvent polled = queue.poll(100);
        assertNull(polled);
    }

    @Test
    void testPollTimeout() throws InterruptedException {
        long start = System.currentTimeMillis();
        CommitEvent polled = queue.poll(200);
        long elapsed = System.currentTimeMillis() - start;

        assertNull(polled);
        assertTrue(elapsed >= 150);
    }

    @Test
    void testIsEmpty() throws InterruptedException {
        assertTrue(queue.isEmpty());

        MessageBO message = MessageMock.createMessage("TEST", 100);
        CommitEvent event = CommitEvent.of(message);
        queue.put(event);

        assertFalse(queue.isEmpty());

        queue.poll();
        assertTrue(queue.isEmpty());
    }

    @Test
    void testFifoOrder() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            MessageBO message = MessageMock.createMessage("TEST", 1, i);
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
    void testMultipleEvents() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            MessageBO message = MessageMock.createMessage("TEST", 100);
            queue.offer(CommitEvent.of(message));
        }

        int count = 0;
        while (!queue.isEmpty() && count < 20) {
            CommitEvent polled = queue.poll();
            if (polled == null) break;
            count++;
        }

        assertEquals(10, count);
    }
}
