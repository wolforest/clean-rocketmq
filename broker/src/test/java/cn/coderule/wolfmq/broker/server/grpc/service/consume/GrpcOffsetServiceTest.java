package cn.coderule.wolfmq.broker.server.grpc.service.consume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcOffsetServiceTest {

    private final GrpcOffsetService service = new GrpcOffsetService();

    @Test
    void testImplementsObject() {
        assertNotNull(service);
    }

    @Test
    void testGetOffsetAsyncReturnsNull() {
        assertNull(service.getOffsetAsync(null, null));
    }

    @Test
    void testQueryOffsetAsyncReturnsNull() {
        assertNull(service.queryOffsetAsync(null, null));
    }

    @Test
    void testUpdateOffsetAsyncReturnsNull() {
        assertNull(service.updateOffsetAsync(null, null));
    }
}