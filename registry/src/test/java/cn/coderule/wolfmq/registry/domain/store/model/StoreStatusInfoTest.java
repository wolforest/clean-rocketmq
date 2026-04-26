package cn.coderule.wolfmq.registry.domain.store.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StoreStatusInfoTest {

    @Test
    void testBuilder() {
        Map<Long, String> brokerAddrs = Map.of(0L, "127.0.0.1:10911", 1L, "127.0.0.1:10912");

        StoreStatusInfo info = StoreStatusInfo.builder()
            .brokerAddrs(brokerAddrs)
            .offlineBrokerAddr("127.0.0.1:10912")
            .haBrokerAddr("127.0.0.1:10911")
            .build();

        assertNotNull(info);
        assertEquals(brokerAddrs, info.getBrokerAddrs());
        assertEquals("127.0.0.1:10912", info.getOfflineBrokerAddr());
        assertEquals("127.0.0.1:10911", info.getHaBrokerAddr());
    }

    @Test
    void testDefaultConstructor() {
        StoreStatusInfo info = new StoreStatusInfo();

        assertNotNull(info);
        assertNull(info.getBrokerAddrs());
        assertNull(info.getOfflineBrokerAddr());
        assertNull(info.getHaBrokerAddr());
    }

    @Test
    void testSettersAndGetters() {
        StoreStatusInfo info = new StoreStatusInfo();
        Map<Long, String> brokerAddrs = Map.of(0L, "127.0.0.1:10911");

        info.setBrokerAddrs(brokerAddrs);
        info.setOfflineBrokerAddr("192.168.1.1:10911");
        info.setHaBrokerAddr("10.0.0.1:10912");

        assertEquals(brokerAddrs, info.getBrokerAddrs());
        assertEquals("192.168.1.1:10911", info.getOfflineBrokerAddr());
        assertEquals("10.0.0.1:10912", info.getHaBrokerAddr());
    }

    @Test
    void testAllArgsConstructor() {
        Map<Long, String> brokerAddrs = Map.of(0L, "127.0.0.1:10911");

        StoreStatusInfo info = new StoreStatusInfo(
            brokerAddrs, "192.168.1.1:10911", "10.0.0.1:10912"
        );

        assertEquals(brokerAddrs, info.getBrokerAddrs());
        assertEquals("192.168.1.1:10911", info.getOfflineBrokerAddr());
        assertEquals("10.0.0.1:10912", info.getHaBrokerAddr());
    }

}
