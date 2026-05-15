package cn.coderule.wolfmq.rpc.common.grpc.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcRelayServiceTest {

    @Test
    void testConstructor() {
        GrpcChannel channel = mock(GrpcChannel.class);
        GrpcRelayService service = new GrpcRelayService(channel);

        assertNotNull(service);
    }
}