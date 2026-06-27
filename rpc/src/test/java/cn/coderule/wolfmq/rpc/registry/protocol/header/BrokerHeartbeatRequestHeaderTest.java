package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokerHeartbeatRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        BrokerHeartbeatRequestHeader header = new BrokerHeartbeatRequestHeader();
        header.setBrokerAddr("127.0.0.1:10911");
        header.setBrokerName("broker-1");
        header.setBrokerId(0L);
        header.setClusterName("cluster-1");

        assertEquals("127.0.0.1:10911", header.getBrokerAddr());
        assertEquals("broker-1", header.getBrokerName());
        assertEquals(0L, header.getBrokerId());
        assertEquals("cluster-1", header.getClusterName());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        BrokerHeartbeatRequestHeader header = new BrokerHeartbeatRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }
}