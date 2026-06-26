package cn.coderule.wolfmq.rpc.registry.protocol.body;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrokerMemberGroupTest {

    @Test
    void gettersAndSetters() {
        BrokerMemberGroup group = new BrokerMemberGroup();
        group.setCluster("cluster-1");
        group.setBrokerName("broker-1");

        assertEquals("cluster-1", group.getCluster());
        assertEquals("broker-1", group.getBrokerName());
    }

    @Test
    void defaultValues() {
        BrokerMemberGroup group = new BrokerMemberGroup();
        assertNotNull(group.getBrokerAddrs());
        assertTrue(group.getBrokerAddrs().isEmpty());
    }

    @Test
    void constructorWithArgs() {
        BrokerMemberGroup group = new BrokerMemberGroup("cluster-1", "broker-1");
        assertEquals("cluster-1", group.getCluster());
        assertEquals("broker-1", group.getBrokerName());
    }

    @Test
    void minimumBrokerId_WhenEmpty() {
        BrokerMemberGroup group = new BrokerMemberGroup();
        assertEquals(0, group.minimumBrokerId());
    }

    @Test
    void minimumBrokerId_WithEntries() {
        BrokerMemberGroup group = new BrokerMemberGroup();
        Map<Long, String> addrs = new HashMap<>();
        addrs.put(2L, "addr2");
        addrs.put(1L, "addr1");
        group.setBrokerAddrs(addrs);
        assertEquals(1L, group.minimumBrokerId());
    }

    @Test
    void equalsAndHashCode() {
        BrokerMemberGroup g1 = new BrokerMemberGroup("c1", "b1");
        BrokerMemberGroup g2 = new BrokerMemberGroup("c1", "b1");
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());
    }

    @Test
    void toString_ShouldContainFields() {
        BrokerMemberGroup group = new BrokerMemberGroup("c1", "b1");
        String str = group.toString();
        assertTrue(str.contains("c1"));
        assertTrue(str.contains("b1"));
    }
}