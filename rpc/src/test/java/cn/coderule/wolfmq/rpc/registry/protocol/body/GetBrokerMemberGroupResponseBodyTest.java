package cn.coderule.wolfmq.rpc.registry.protocol.body;

import cn.coderule.wolfmq.domain.domain.cluster.server.BrokerInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetBrokerMemberGroupResponseBodyTest {

    @Test
    void defaultConstructor_ShouldCreate() {
        GetBrokerMemberGroupResponseBody body = new GetBrokerMemberGroupResponseBody();
        assertNotNull(body);
    }
}