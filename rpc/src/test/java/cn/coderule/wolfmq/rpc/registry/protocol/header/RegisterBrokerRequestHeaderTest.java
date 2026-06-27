package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterBrokerRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        RegisterBrokerRequestHeader header = new RegisterBrokerRequestHeader();
        header.setBrokerName("broker-1");
        header.setBrokerAddr("127.0.0.1:10911");
        header.setClusterName("cluster-1");
        header.setHaServerAddr("127.0.0.1:10912");
        header.setBrokerId(0L);
        header.setEnableActingMaster(true);

        assertEquals("broker-1", header.getBrokerName());
        assertEquals("127.0.0.1:10911", header.getBrokerAddr());
        assertEquals("cluster-1", header.getClusterName());
        assertEquals("127.0.0.1:10912", header.getHaServerAddr());
        assertEquals(0L, header.getBrokerId());
        assertTrue(header.getEnableActingMaster());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        RegisterBrokerRequestHeader header = new RegisterBrokerRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }
}