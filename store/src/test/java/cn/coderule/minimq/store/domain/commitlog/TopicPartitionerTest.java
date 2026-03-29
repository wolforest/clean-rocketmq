package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.store.domain.commitlog.sharding.TopicPartitioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicPartitionerTest {

    private static final int MAX_SHARDING = 100;
    private static final int SHARDING = 5;

    private CommitConfig commitConfig;
    private TopicPartitioner partitioner;

    @BeforeEach
    void setUp() {
        commitConfig = new CommitConfig();
        commitConfig.setMaxShardingNumber(MAX_SHARDING);
        commitConfig.setShardingNumber(SHARDING);
        partitioner = new TopicPartitioner(commitConfig);
    }

    @Test
    void testConstructor_WithConfigOnly() {
        TopicPartitioner p = new TopicPartitioner(commitConfig);
        assertNotNull(p);
    }

    @Test
    void testConstructor_WithConfigAndTopicService() {
        TopicPartitioner p = new TopicPartitioner(commitConfig, null);
        assertNotNull(p);
    }

    @Test
    void testPartitionByTopic_ValidTopic() {
        int partition1 = partitioner.partitionByTopic("topic1");
        int partition2 = partitioner.partitionByTopic("topic2");

        assertTrue(partition1 >= 0);
        assertTrue(partition1 < SHARDING);
        assertTrue(partition2 >= 0);
        assertTrue(partition2 < SHARDING);
    }

    @Test
    void testPartitionByTopic_SameTopicSamePartition() {
        int partition1 = partitioner.partitionByTopic("SAME_TOPIC");
        int partition2 = partitioner.partitionByTopic("SAME_TOPIC");

        assertEquals(partition1, partition2);
    }

    @Test
    void testPartitionByTopic_DifferentTopicsMayHaveDifferentPartitions() {
        int partition1 = partitioner.partitionByTopic("TOPIC_A");
        int partition2 = partitioner.partitionByTopic("TOPIC_B");

        assertTrue(partition1 >= 0 && partition1 < SHARDING);
        assertTrue(partition2 >= 0 && partition2 < SHARDING);
    }

    @Test
    void testPartitionByTopic_BlankTopic() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> partitioner.partitionByTopic("")
        );
        assertEquals("topic can't be blank", ex.getMessage());
    }

    @Test
    void testPartitionByTopic_NullTopic() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> partitioner.partitionByTopic(null)
        );
        assertEquals("topic can't be blank", ex.getMessage());
    }

    @Test
    void testPartitionByTopic_WhitespaceTopic() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> partitioner.partitionByTopic("   ")
        );
        assertEquals("topic can't be blank", ex.getMessage());
    }

    @Test
    void testPartitionByOffset_ZeroOffset() {
        int partition = partitioner.partitionByOffset(0);
        assertEquals(0, partition);
    }

    @Test
    void testPartitionByOffset_PositiveOffset() {
        int partition1 = partitioner.partitionByOffset(100);
        int partition2 = partitioner.partitionByOffset(250);
        int partition3 = partitioner.partitionByOffset(5008310);
        int partition4 = partitioner.partitionByOffset(5128320);
        int partition5 = partitioner.partitionByOffset(5128003);

        assertTrue(partition1 >= 0 && partition1 < MAX_SHARDING);
        assertTrue(partition2 >= 0 && partition2 < MAX_SHARDING);
        assertTrue(partition3 >= 0 && partition3 < MAX_SHARDING);
        assertTrue(partition4 >= 0 && partition4 < MAX_SHARDING);
        assertTrue(partition5 >= 0 && partition5 < MAX_SHARDING);

        assertEquals(partition1, 0);
        assertEquals(partition2, 50);
        assertEquals(partition3, 10);
        assertEquals(partition4, 20);
        assertEquals(partition5, 3);
    }

    @Test
    void testPartitionByOffset_LargeOffset() {
        long largeOffset = Long.MAX_VALUE / 10;
        int partition = partitioner.partitionByOffset(largeOffset);

        assertTrue(partition >= 0 && partition < MAX_SHARDING);
    }

    @Test
    void testPartitionByOffset_NegativeOffset() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> partitioner.partitionByOffset(-1)
        );
        assertEquals("offset must be positive", ex.getMessage());
    }

    @Test
    void testPartitionByOffset_MultipleOffsets() {
        for (long offset = 0; offset < 1000; offset++) {
            int partition = partitioner.partitionByOffset(offset);
            assertTrue(partition >= 0 && partition < MAX_SHARDING,
                "Partition out of range for offset " + offset);
        }
    }

    @Test
    void testPartitionByOffset_ConsistentWithMath() {
        long offset = 54321;
        int expectedPartition = (int) (offset % MAX_SHARDING);

        int actualPartition = partitioner.partitionByOffset(offset);
        assertEquals(expectedPartition, actualPartition);
    }

    @Test
    void testPartitionByTopic_ConsistentWithHashCode() {
        String topic = "CONSISTENT_TOPIC";
        int expectedPartition = Math.abs(topic.hashCode()) % MAX_SHARDING % SHARDING;

        int actualPartition = partitioner.partitionByTopic(topic);
        assertEquals(expectedPartition, actualPartition);
    }

    @Test
    void testPartitionByOffset_ConsecutiveOffsets() {
        int lastPartition = -1;
        for (int i = 0; i < MAX_SHARDING * 3; i++) {
            int partition = partitioner.partitionByOffset(i);
            assertTrue(partition >= 0 && partition < MAX_SHARDING);

            if (i > 0 && partition != lastPartition) {
                assertEquals(i % MAX_SHARDING, partition);
            }
            lastPartition = partition;
        }
    }

    @Test
    void testPartitionByTopic_UnicodeTopic() {
        int partition = partitioner.partitionByTopic("中文主题");
        assertTrue(partition >= 0 && partition < SHARDING);
    }

    @Test
    void testPartitionByTopic_LongTopic() {
        String longTopic = "LONG_TOPIC_PART_".repeat(1000);

        int partition = partitioner.partitionByTopic(longTopic);
        assertTrue(partition >= 0 && partition < SHARDING);
    }

    @Test
    void testPartitionByTopic_SpecialCharacters() {
        int partition = partitioner.partitionByTopic("topic-with-special!@#$%");
        assertTrue(partition >= 0 && partition < SHARDING);
    }

    @Test
    void testPartitionByTopic_WithDifferentShardingNumber() {
        commitConfig.setShardingNumber(3);
        TopicPartitioner p = new TopicPartitioner(commitConfig);

        for (int i = 0; i < 100; i++) {
            int partition = p.partitionByTopic("TOPIC_" + i);
            assertTrue(partition >= 0 && partition < 3,
                "Partition out of range: " + partition);
        }
    }

    @Test
    void testPartitionByOffset_WithDifferentMaxShardingNumber() {
        commitConfig.setMaxShardingNumber(50);
        TopicPartitioner partitionerWith50 = new TopicPartitioner(commitConfig);

        for (long offset = 0; offset < 1000; offset++) {
            int partition = partitionerWith50.partitionByOffset(offset);
            assertTrue(partition >= 0 && partition < 50,
                "Partition out of range: " + partition);
        }
    }
}
