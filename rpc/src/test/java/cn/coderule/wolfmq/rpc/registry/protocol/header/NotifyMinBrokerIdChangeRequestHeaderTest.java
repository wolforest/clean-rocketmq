package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotifyMinBrokerIdChangeRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        NotifyMinBrokerIdChangeRequestHeader header = new NotifyMinBrokerIdChangeRequestHeader();
        header.setMinBrokerId(1L);
        header.setBrokerName("broker-1");
        header.setMinBrokerAddr("127.0.0.1:10911");
        header.setOfflineBrokerAddr("127.0.0.1:10912");
        header.setHaBrokerAddr("127.0.0.1:10913");

        assertEquals(1L, header.getMinBrokerId());
        assertEquals("broker-1", header.getBrokerName());
        assertEquals("127.0.0.1:10911", header.getMinBrokerAddr());
        assertEquals("127.0.0.1:10912", header.getOfflineBrokerAddr());
        assertEquals("127.0.0.1:10913", header.getHaBrokerAddr());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        NotifyMinBrokerIdChangeRequestHeader header = new NotifyMinBrokerIdChangeRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }

    @Test
    void defaultValues_ShouldBeNull() {
        NotifyMinBrokerIdChangeRequestHeader header = new NotifyMinBrokerIdChangeRequestHeader();
        assertNull(header.getMinBrokerId());
        assertNull(header.getBrokerName());
        assertNull(header.getMinBrokerAddr());
        assertNull(header.getOfflineBrokerAddr());
        assertNull(header.getHaBrokerAddr());
    }
}