package cn.coderule.wolfmq.rpc.common.grpc.channel;

import io.netty.channel.ChannelId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GrpcChannelIdTest {

    @Test
    void testConstructor() {
        String clientId = "test-client-123";
        GrpcChannelId id = new GrpcChannelId(clientId);
        
        assertNotNull(id);
        assertEquals(clientId, id.asShortText());
        assertEquals(clientId, id.asLongText());
    }

    @Test
    void testAsShortText() {
        String clientId = "short-id";
        GrpcChannelId id = new GrpcChannelId(clientId);
        
        assertEquals(clientId, id.asShortText());
    }

    @Test
    void testAsLongText() {
        String clientId = "long-id-string";
        GrpcChannelId id = new GrpcChannelId(clientId);
        
        assertEquals(clientId, id.asLongText());
    }

    @Test
    void testCompareToSameInstance() {
        String clientId = "test-id";
        GrpcChannelId id = new GrpcChannelId(clientId);
        
        assertEquals(0, id.compareTo(id));
    }

    @Test
    void testCompareToSameGrpcChannelId() {
        GrpcChannelId id1 = new GrpcChannelId("client-a");
        GrpcChannelId id2 = new GrpcChannelId("client-b");
        GrpcChannelId id3 = new GrpcChannelId("client-a");
        
        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertEquals(0, id1.compareTo(id3));
    }

    @Test
    void testCompareToDifferentChannelIdType() {
        GrpcChannelId grpcId = new GrpcChannelId("grpc-client");
        ChannelId mockId = mock(ChannelId.class);
        when(mockId.asLongText()).thenReturn("mock-client");
        
        int result = grpcId.compareTo(mockId);
        
        assertTrue(result != 0);
    }

    @Test
    void testCompareToWithSameLongText() {
        GrpcChannelId grpcId = new GrpcChannelId("same-text");
        ChannelId mockId = mock(ChannelId.class);
        when(mockId.asLongText()).thenReturn("same-text");
        
        assertEquals(0, grpcId.compareTo(mockId));
    }
}
