package cn.coderule.wolfmq.rpc.common.rpc.core.constant;

import io.netty.util.AttributeKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpcKeysTest {

    @Test
    void allAttributeKeys_ShouldBeDefined() {
        assertNotNull(RpcKeys.REMOTE_ADDR_KEY);
        assertNotNull(RpcKeys.CLIENT_ID_KEY);
        assertNotNull(RpcKeys.VERSION_KEY);
        assertNotNull(RpcKeys.LANGUAGE_CODE_KEY);
        assertNotNull(RpcKeys.PROXY_PROTOCOL_ADDR);
        assertNotNull(RpcKeys.PROXY_PROTOCOL_PORT);
        assertNotNull(RpcKeys.PROXY_PROTOCOL_SERVER_ADDR);
        assertNotNull(RpcKeys.PROXY_PROTOCOL_SERVER_PORT);
    }

    @Test
    void remoteAddrKey_NameShouldMatch() {
        assertEquals("RemoteAddr", RpcKeys.REMOTE_ADDR_KEY.name());
    }

    @Test
    void clientIdKey_NameShouldMatch() {
        assertEquals("ClientId", RpcKeys.CLIENT_ID_KEY.name());
    }

    @Test
    void valueOf_ShouldReturnAttributeKey() {
        AttributeKey<String> key = RpcKeys.valueOf("TestKey");
        assertNotNull(key);
        assertEquals("TestKey", key.name());
    }

    @Test
    void valueOf_SameName_ShouldReturnSameKey() {
        AttributeKey<String> key1 = RpcKeys.valueOf("SameKey");
        AttributeKey<String> key2 = RpcKeys.valueOf("SameKey");
        assertSame(key1, key2);
    }
}