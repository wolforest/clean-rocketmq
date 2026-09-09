package cn.coderule.wolfmq.rpc.common.grpc.core.constants;

import io.grpc.Attributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcKeysTest {

    @Test
    void channelId_ShouldBeDefined() {
        assertNotNull(GrpcKeys.CHANNEL_ID);
    }

    @Test
    void proxyProtocolAddr_ShouldBeDefined() {
        assertNotNull(GrpcKeys.PROXY_PROTOCOL_ADDR);
    }

    @Test
    void proxyProtocolPort_ShouldBeDefined() {
        assertNotNull(GrpcKeys.PROXY_PROTOCOL_PORT);
    }

    @Test
    void channelIdAttribute_ShouldBeUnique() {
        Attributes.Key<String> key1 = GrpcKeys.CHANNEL_ID;
        Attributes.Key<String> key2 = Attributes.Key.create("channel-id");
        assertNotEquals(key1, key2);
    }
}