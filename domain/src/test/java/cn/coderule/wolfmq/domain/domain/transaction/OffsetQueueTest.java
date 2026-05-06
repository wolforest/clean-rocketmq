package cn.coderule.wolfmq.domain.domain.transaction;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OffsetQueueTest {

    @Test
    void offer_and_poll() throws InterruptedException {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        assertTrue(queue.offer("100", 1000));
        String result = queue.poll();
        assertEquals("100", result);
    }

    @Test
    void isEmpty_initially() {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        assertTrue(queue.isEmpty());
    }

    @Test
    void isEmpty_afterOfferAndAdd() throws InterruptedException {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        queue.offer("100", 1000);
        queue.addAndGet(1);
        assertFalse(queue.isEmpty());
    }

    @Test
    void isEmpty_afterPoll() throws InterruptedException {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        queue.offer("100", 1000);
        queue.poll();
        assertTrue(queue.isEmpty());
    }

    @Test
    void addAndGet() {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        assertEquals(5, queue.addAndGet(5));
        assertEquals(8, queue.addAndGet(3));
        assertEquals(6, queue.addAndGet(-2));
    }

    @Test
    void getTotalSize() {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        assertEquals(0, queue.getTotalSize());
        queue.addAndGet(10);
        assertEquals(10, queue.getTotalSize());
    }

    @Test
    void offer_fullQueue_returnsFalse() throws InterruptedException {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 2);
        assertTrue(queue.offer("1", 1000));
        assertTrue(queue.offer("2", 1000));
        assertFalse(queue.offer("3", 10));
    }

    @Test
    void poll_empty_returnsNull() {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        assertNull(queue.poll());
    }

    @Test
    void fifo_order() throws InterruptedException {
        OffsetQueue queue = new OffsetQueue(System.currentTimeMillis(), 100);
        queue.offer("first", 1000);
        queue.offer("second", 1000);
        queue.offer("third", 1000);
        assertEquals("first", queue.poll());
        assertEquals("second", queue.poll());
        assertEquals("third", queue.poll());
    }
}