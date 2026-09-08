package cn.coderule.wolfmq.rpc.common.grpc.core.constants;

import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcConstantsTest {

    @Test
    void metadataKey_ShouldBeDefined() {
        assertNotNull(GrpcConstants.REMOTE_ADDRESS);
        assertNotNull(GrpcConstants.LOCAL_ADDRESS);
        assertNotNull(GrpcConstants.AUTHORIZATION);
        assertNotNull(GrpcConstants.NAMESPACE_ID);
        assertNotNull(GrpcConstants.DATE_TIME);
        assertNotNull(GrpcConstants.REQUEST_ID);
        assertNotNull(GrpcConstants.LANGUAGE);
        assertNotNull(GrpcConstants.CLIENT_VERSION);
        assertNotNull(GrpcConstants.PROTOCOL_VERSION);
        assertNotNull(GrpcConstants.RPC_NAME);
        assertNotNull(GrpcConstants.SIMPLE_RPC_NAME);
        assertNotNull(GrpcConstants.SESSION_TOKEN);
        assertNotNull(GrpcConstants.CLIENT_ID);
        assertNotNull(GrpcConstants.AUTHORIZATION_AK);
        assertNotNull(GrpcConstants.CHANNEL_ID);
    }

    @Test
    void metadataKey_NameShouldMatch() {
        assertEquals("rpc-remote-address", GrpcConstants.REMOTE_ADDRESS.name());
        assertEquals("rpc-local-address", GrpcConstants.LOCAL_ADDRESS.name());
        assertEquals("authorization", GrpcConstants.AUTHORIZATION.name());
        assertEquals("x-mq-namespace", GrpcConstants.NAMESPACE_ID.name());
        assertEquals("x-mq-client-id", GrpcConstants.CLIENT_ID.name());
    }

    @Test
    void contextKey_ShouldBeDefined() {
        Metadata metadata = new Metadata();
        assertNotNull(GrpcConstants.METADATA);
        assertNull(GrpcConstants.METADATA.get());
    }
}