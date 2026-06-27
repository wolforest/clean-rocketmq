package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetRouteInfoRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        GetRouteInfoRequestHeader header = new GetRouteInfoRequestHeader();
        header.setTopic("test-topic");

        assertEquals("test-topic", header.getTopic());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        GetRouteInfoRequestHeader header = new GetRouteInfoRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }
}