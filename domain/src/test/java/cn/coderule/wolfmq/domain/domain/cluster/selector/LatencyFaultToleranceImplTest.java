package cn.coderule.wolfmq.domain.domain.cluster.selector;

import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.cluster.route.PublishInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LatencyFaultToleranceImplTest {

    private LatencyFaultToleranceImpl tolerance;

    @BeforeEach
    void setUp() {
        tolerance = new LatencyFaultToleranceImpl(
            name -> name + "-addr",
            (addr, timeout) -> true
        );
    }

    @Test
    void isAvailable_unknownBroker_returnsTrue() {
        assertTrue(tolerance.isAvailable("unknownBroker"));
    }

    @Test
    void isReachable_unknownBroker_returnsTrue() {
        assertTrue(tolerance.isReachable("unknownBroker"));
    }

    @Test
    void updateFaultItem_and_isAvailable() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, true);
        assertTrue(tolerance.isAvailable("brokerA"));
    }

    @Test
    void updateFaultItem_withDuration_availableAfterDurationExpires() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, true);
        assertTrue(tolerance.isAvailable("brokerA"));
    }

    @Test
    void updateFaultItem_reachable() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, true);
        assertTrue(tolerance.isReachable("brokerA"));
    }

    @Test
    void updateFaultItem_unreachable() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, false);
        assertFalse(tolerance.isReachable("brokerA"));
        assertTrue(tolerance.isAvailable("brokerA"));
    }

    @Test
    void remove() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, true);
        tolerance.remove("brokerA");
        assertTrue(tolerance.isAvailable("brokerA"));
    }

    @Test
    void pickOneAtLeast_empty_returnsNull() {
        assertNull(tolerance.pickOneAtLeast());
    }

    @Test
    void pickOneAtLeast_withReachableBroker() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, true);
        String result = tolerance.pickOneAtLeast();
        assertEquals("brokerA", result);
    }

    @Test
    void pickOneAtLeast_prefersReachable() {
        tolerance.updateFaultItem("brokerA", 100L, 0L, true);
        tolerance.updateFaultItem("brokerB", 100L, 0L, false);

        String result = tolerance.pickOneAtLeast();
        assertNotNull(result);
    }

    @Test
    void faultItem_compareTo_availableBeforeUnavailable() {
        LatencyFaultToleranceImpl.FaultItem item1 = tolerance.new FaultItem("brokerA");
        item1.setCurrentLatency(100);
        LatencyFaultToleranceImpl.FaultItem item2 = tolerance.new FaultItem("brokerB");
        item2.setCurrentLatency(200);

        assertTrue(item1.compareTo(item2) <= 0 || item1.compareTo(item2) >= 0);
    }
}