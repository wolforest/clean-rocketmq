package cn.coderule.wolfmq.broker.domain.transaction.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMetricsTest {

    private TransactionMetrics metrics;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        String configPath = tempDir.toString() + "/metrics.json";
        metrics = new TransactionMetrics(configPath);
    }

    @Test
    void testAddAndGet() {
        metrics.addAndGet("topic1", 5);
        assertEquals(5, metrics.getTransactionCount("topic1"));

        metrics.addAndGet("topic1", 3);
        assertEquals(8, metrics.getTransactionCount("topic1"));
    }

    @Test
    void testGetTransactionCountNonExistent() {
        assertEquals(0, metrics.getTransactionCount("nonexistent"));
    }

    @Test
    void testAddMultipleTopics() {
        metrics.addAndGet("topic1", 5);
        metrics.addAndGet("topic2", 10);

        assertEquals(5, metrics.getTransactionCount("topic1"));
        assertEquals(10, metrics.getTransactionCount("topic2"));
    }

    @Test
    void testGetTopicPair() {
        Metric metric = metrics.getTopicPair("topic1");
        assertNotNull(metric);
        assertEquals(0, metric.getCount().get());

        metric.getCount().addAndGet(3);
        Metric sameMetric = metrics.getTopicPair("topic1");
        assertEquals(3, sameMetric.getCount().get());
    }

    @Test
    void testCleanMetricsRemovesSpecifiedTopics() {
        metrics.addAndGet("topic1", 5);
        metrics.addAndGet("topic2", 10);

        metrics.cleanMetrics(Set.of("topic1"));

        assertEquals(0, metrics.getTransactionCount("topic1"));
        assertEquals(10, metrics.getTransactionCount("topic2"));
    }

    @Test
    void testCleanMetricsEmptySet() {
        metrics.addAndGet("topic1", 5);
        metrics.cleanMetrics(Set.of());
        assertEquals(5, metrics.getTransactionCount("topic1"));
    }

    @Test
    void testCleanMetricsNullSet() {
        metrics.addAndGet("topic1", 5);
        metrics.cleanMetrics(null);
        assertEquals(5, metrics.getTransactionCount("topic1"));
    }

    @Test
    void testConfigFilePath() {
        assertTrue(metrics.configFilePath().endsWith("metrics.json"));
    }

    @Test
    void testGetDataVersion() {
        assertNotNull(metrics.getDataVersion());
    }
}
