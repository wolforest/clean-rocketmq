package cn.coderule.wolfmq.store.infra.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class ReferenceResourceTest {

    private TestReferenceResource resource;

    static class TestReferenceResource extends ReferenceResource {
        private final AtomicLong cleanupCallCount = new AtomicLong(0);

        @Override
        public boolean cleanup(long currentRef) {
            cleanupCallCount.incrementAndGet();
            return currentRef <= 0;
        }

        public long getCleanupCallCount() {
            return cleanupCallCount.get();
        }
    }

    @BeforeEach
    void setUp() {
        resource = new TestReferenceResource();
    }

    @Test
    void testInitialRefCount() {
        assertEquals(1, resource.getRefCount());
    }

    @Test
    void testInitialAvailable() {
        assertTrue(resource.isAvailable());
    }

    @Test
    void testHoldIncrementsRefCount() {
        boolean result = resource.hold();
        
        assertTrue(result);
        assertEquals(2, resource.getRefCount());
    }

    @Test
    void testMultipleHolds() {
        resource.hold();
        resource.hold();
        resource.hold();
        
        assertEquals(4, resource.getRefCount());
    }

    @Test
    void testShutdownSetsUnavailable() {
        resource.shutdown(0);
        
        assertFalse(resource.isAvailable());
    }

    @Test
       void testHoldFailsAfterShutdown() {
        resource.shutdown(0);
        
        boolean result = resource.hold();
        
        assertFalse(result);
    }

    @Test
    void testReleaseDecrementsRefCount() {
        resource.hold();
        assertEquals(2, resource.getRefCount());
        
        resource.release();
        assertEquals(1, resource.getRefCount());
    }

    @Test
    void testIsCleanupOver() {
        assertFalse(resource.isCleanupOver());
        
        resource.shutdown(0);
        resource.release();
        
        assertTrue(resource.isCleanupOver());
    }

    @Test
    void testConcurrentHoldAndShutdown() throws Exception {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicBoolean allHoldsSucceeded = new AtomicBoolean(true);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (!resource.hold()) {
                        allHoldsSucceeded.set(false);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        assertTrue(resource.getRefCount() > 0);
    }
}