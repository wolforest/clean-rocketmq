package cn.coderule.wolfmq.rpc.common.core.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChannelProtocolTypeTest {

    @Test
    void unknownHasCorrectName() {
        assertEquals("unknown", ChannelProtocolType.UNKNOWN.getName());
    }

    @Test
    void grpcV2HasCorrectName() {
        assertEquals("grpc_v2", ChannelProtocolType.GRPC_V2.getName());
    }

    @Test
    void grpcV1HasCorrectName() {
        assertEquals("grpc_v1", ChannelProtocolType.GRPC_V1.getName());
    }

    @Test
    void remotingHasCorrectName() {
        assertEquals("remoting", ChannelProtocolType.REMOTING.getName());
    }

    @Test
    void valuesHasFourEntries() {
        assertEquals(4, ChannelProtocolType.values().length);
    }
}