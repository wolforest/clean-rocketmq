package cn.coderule.wolfmq.domain.domain.consumer.consume;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InflightCounterTest {

    private InflightCounter counter;

    @BeforeEach
    void setUp() {
        counter = new InflightCounter();
    }

    @Test
    void increment_and_get() {
        counter.increment("topicA", "groupA", 0, 5);
        assertEquals(5, counter.get("topicA", "groupA", 0));
    }

    @Test
    void increment_multipleTimes() {
        counter.increment("topicA", "groupA", 0, 3);
        counter.increment("topicA", "groupA", 0, 2);
        assertEquals(5, counter.get("topicA", "groupA", 0));
    }

    @Test
    void increment_differentQueues() {
        counter.increment("topicA", "groupA", 0, 3);
        counter.increment("topicA", "groupA", 1, 2);
        assertEquals(3, counter.get("topicA", "groupA", 0));
        assertEquals(2, counter.get("topicA", "groupA", 1));
    }

    @Test
    void increment_differentGroups() {
        counter.increment("topicA", "groupA", 0, 3);
        counter.increment("topicA", "groupB", 0, 2);
        assertEquals(3, counter.get("topicA", "groupA", 0));
        assertEquals(2, counter.get("topicA", "groupB", 0));
    }

    @Test
    void increment_zeroOrNegative_noop() {
        counter.increment("topicA", "groupA", 0, 0);
        assertEquals(0, counter.get("topicA", "groupA", 0));

        counter.increment("topicA", "groupA", 0, -1);
        assertEquals(0, counter.get("topicA", "groupA", 0));
    }

    @Test
    void decrement() {
        counter.increment("topicA", "groupA", 0, 10);
        counter.decrement("topicA", "groupA", 1000L, 0, 3);
        assertEquals(7, counter.get("topicA", "groupA", 0));
    }

    @Test
    void decrement_toZero_removesEntry() {
        counter.increment("topicA", "groupA", 0, 5);
        counter.decrement("topicA", "groupA", 1000L, 0, 5);
        assertEquals(0, counter.get("topicA", "groupA", 0));
    }

    @Test
    void decrement_nonExistent_returnsZero() {
        assertEquals(0, counter.get("nonTopic", "nonGroup", 0));
    }

    @Test
    void clearByGroup() {
        counter.increment("topicA", "groupA", 0, 5);
        counter.increment("topicA", "groupA", 1, 3);
        counter.increment("topicA", "groupB", 0, 2);

        counter.clearByGroup("groupA");
        assertEquals(0, counter.get("topicA", "groupA", 0));
        assertEquals(0, counter.get("topicA", "groupA", 1));
        assertEquals(2, counter.get("topicA", "groupB", 0));
    }

    @Test
    void clearByTopic() {
        counter.increment("topicA", "groupA", 0, 5);
        counter.increment("topicB", "groupA", 0, 3);

        counter.clearByTopic("topicA");
        assertEquals(0, counter.get("topicA", "groupA", 0));
        assertEquals(3, counter.get("topicB", "groupA", 0));
    }

    @Test
    void clear_specificQueue() {
        counter.increment("topicA", "groupA", 0, 5);
        counter.increment("topicA", "groupA", 1, 3);

        counter.clear("topicA", "groupA", 0);
        assertEquals(0, counter.get("topicA", "groupA", 0));
        assertEquals(3, counter.get("topicA", "groupA", 1));
    }
}