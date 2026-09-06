package cn.coderule.wolfmq.rpc.common.core.channel.remote;

import cn.coderule.wolfmq.rpc.common.core.channel.ChannelProtocolType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteChannelSerializerTest {

    @Test
    void toJson_ShouldSerializeAllFields() {
        RemoteChannel channel = new RemoteChannel(
            "proxy-host", "192.168.1.1:10911", "127.0.0.1:10912",
            ChannelProtocolType.GRPC_V2, "extend-attr");

        String json = RemoteChannelSerializer.toJson(channel);

        assertNotNull(json);
        assertTrue(json.contains("proxy-host"));
        assertTrue(json.contains("192.168.1.1:10911"));
        assertTrue(json.contains("127.0.0.1:10912"));
        assertTrue(json.contains("GRPC"));
        assertTrue(json.contains("extend-attr"));
    }

    @Test
    void decodeFromJson_ShouldDeserializeAllFields() {
        RemoteChannel original = new RemoteChannel(
            "proxy-host", "192.168.1.1:10911", "127.0.0.1:10912",
            ChannelProtocolType.REMOTING, "test-attr");

        String json = RemoteChannelSerializer.toJson(original);
        RemoteChannel decoded = RemoteChannelSerializer.decodeFromJson(json);

        assertNotNull(decoded);
        assertEquals(original.getRemoteProxyIp(), decoded.getRemoteProxyIp());
        assertEquals(original.getRemoteAddress(), decoded.getRemoteAddress());
        assertEquals(original.getLocalAddress(), decoded.getLocalAddress());
        assertEquals(original.getType(), decoded.getType());
    }

    @Test
    void decodeFromJson_WithBlank_ShouldReturnNull() {
        assertNull(RemoteChannelSerializer.decodeFromJson(""));
        assertNull(RemoteChannelSerializer.decodeFromJson(null));
    }

    @Test
    void decodeFromJson_WithInvalidJson_ShouldReturnNull() {
        assertNull(RemoteChannelSerializer.decodeFromJson("not valid json"));
    }

    @Test
    void roundTrip_ShouldPreserveData() {
        RemoteChannel original = new RemoteChannel(
            "my-proxy", "10.0.0.1:8080", "10.0.0.2:9090",
            ChannelProtocolType.GRPC_V2, "some-attr");

        String json = RemoteChannelSerializer.toJson(original);
        RemoteChannel restored = RemoteChannelSerializer.decodeFromJson(json);

        assertEquals(original.getRemoteProxyIp(), restored.getRemoteProxyIp());
        assertEquals(original.getRemoteAddress(), restored.getRemoteAddress());
        assertEquals(original.getLocalAddress(), restored.getLocalAddress());
    }
}