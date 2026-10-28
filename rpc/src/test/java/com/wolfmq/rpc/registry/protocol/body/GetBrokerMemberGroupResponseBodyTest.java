package com.wolfmq.rpc.registry.protocol.body;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetBrokerMemberGroupResponseBodyTest {

    @Test
    void defaultConstructor_ShouldCreate() {
        GetBrokerMemberGroupResponseBody body = new GetBrokerMemberGroupResponseBody();
        assertNotNull(body);
    }
}
