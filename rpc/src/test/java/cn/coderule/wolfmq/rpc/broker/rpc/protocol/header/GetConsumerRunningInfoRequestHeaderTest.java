package cn.coderule.wolfmq.rpc.broker.rpc.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetConsumerRunningInfoRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        GetConsumerRunningInfoRequestHeader header = new GetConsumerRunningInfoRequestHeader();
        header.setConsumerGroup("group-1");
        header.setClientId("client-1");
        header.setJstackEnable(true);

        assertEquals("group-1", header.getConsumerGroup());
        assertEquals("client-1", header.getClientId());
        assertTrue(header.isJstackEnable());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        GetConsumerRunningInfoRequestHeader header = new GetConsumerRunningInfoRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }
}