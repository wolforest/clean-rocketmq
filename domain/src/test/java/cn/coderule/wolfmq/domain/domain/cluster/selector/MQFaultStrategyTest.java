package cn.coderule.wolfmq.domain.domain.cluster.selector;

import cn.coderule.wolfmq.domain.domain.MessageQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MQFaultStrategyTest {

    private MQFaultStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MQFaultStrategy(
            name -> name + "-addr",
            (addr, timeout) -> true
        );
    }

    @Test
    void faultToleranceDisabled_whenDisabled() {
        strategy.setSendLatencyFaultEnable(false);
        assertFalse(strategy.isSendLatencyFaultEnable());
    }

    @Test
    void faultToleranceEnabled_whenEnabled() {
        strategy.setSendLatencyFaultEnable(true);
        assertTrue(strategy.isSendLatencyFaultEnable());
    }

    @Test
    void brokerFilter_filtersByBrokerName() {
        MQFaultStrategy.BrokerFilter filter = new MQFaultStrategy.BrokerFilter();
        MessageQueue mq1 = MessageQueue.builder().topicName("topic").groupName("brokerA").queueId(0).build();
        MessageQueue mq2 = MessageQueue.builder().topicName("topic").groupName("brokerB").queueId(0).build();

        filter.setLastBrokerName("brokerA");
        assertFalse(filter.filter(mq1));
        assertTrue(filter.filter(mq2));
    }

    @Test
    void brokerFilter_nullLastBrokerName_passesAll() {
        MQFaultStrategy.BrokerFilter filter = new MQFaultStrategy.BrokerFilter();
        MessageQueue mq = MessageQueue.builder().topicName("topic").groupName("brokerA").queueId(0).build();
        assertTrue(filter.filter(mq));
    }

    @Test
    void getNotAvailableDuration_returnsArray() {
        long[] durations = strategy.getNotAvailableDuration();
        assertNotNull(durations);
        assertEquals(7, durations.length);
    }

    @Test
    void getLatencyMax_returnsArray() {
        long[] latencyMax = strategy.getLatencyMax();
        assertNotNull(latencyMax);
        assertEquals(7, latencyMax.length);
    }

    @Test
    void setNotAvailableDuration() {
        long[] newDurations = {0L, 0L, 1000L, 3000L, 5000L, 8000L, 20000L};
        strategy.setNotAvailableDuration(newDurations);
        assertArrayEquals(newDurations, strategy.getNotAvailableDuration());
    }

    @Test
    void setLatencyMax() {
        long[] newLatency = {50L, 100L, 500L, 1000L, 2000L, 4000L, 10000L};
        strategy.setLatencyMax(newLatency);
        assertArrayEquals(newLatency, strategy.getLatencyMax());
    }
}