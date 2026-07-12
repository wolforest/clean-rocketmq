package cn.coderule.wolfmq.rpc.broker.rpc.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumeMessageDirectlyResultRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        ConsumeMessageDirectlyResultRequestHeader header = new ConsumeMessageDirectlyResultRequestHeader();
        header.setConsumerGroup("group-1");
        header.setClientId("client-1");
        header.setTopic("topic-1");

        assertEquals("group-1", header.getConsumerGroup());
        assertEquals("client-1", header.getClientId());
        assertEquals("topic-1", header.getTopic());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        ConsumeMessageDirectlyResultRequestHeader header = new ConsumeMessageDirectlyResultRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }
}