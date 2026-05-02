package cn.coderule.wolfmq.broker.domain.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BrokerOffsetServiceTest {

    @Test
    void testInstantiation() {
        BrokerOffsetService service = new BrokerOffsetService();
        assertNotNull(service);
    }
}