package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetBrokerMemberGroupRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        GetBrokerMemberGroupRequestHeader header = new GetBrokerMemberGroupRequestHeader();
        header.setClusterName("cluster-1");
        header.setBrokerName("broker-1");

        assertEquals("cluster-1", header.getClusterName());
        assertEquals("broker-1", header.getBrokerName());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        GetBrokerMemberGroupRequestHeader header = new GetBrokerMemberGroupRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }
}