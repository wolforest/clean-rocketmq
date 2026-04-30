package cn.coderule.wolfmq.broker.domain.transaction.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricTest {

    @Test
    void testConstructor() {
        Metric metric = new Metric();
        assertNotNull(metric.getCount());
        assertEquals(0, metric.getCount().get());
        assertTrue(metric.getTimeStamp() > 0);
    }

    @Test
    void testIncrementCount() {
        Metric metric = new Metric();
        metric.getCount().incrementAndGet();
        assertEquals(1, metric.getCount().get());

        metric.getCount().addAndGet(5);
        assertEquals(6, metric.getCount().get());
    }

    @Test
    void testSetTimeStamp() {
        Metric metric = new Metric();
        metric.setTimeStamp(123456789L);
        assertEquals(123456789L, metric.getTimeStamp());
    }

    @Test
    void testToString() {
        Metric metric = new Metric();
        metric.getCount().set(42);
        String str = metric.toString();
        assertTrue(str.startsWith("["));
        assertTrue(str.contains("42"));
    }
}
