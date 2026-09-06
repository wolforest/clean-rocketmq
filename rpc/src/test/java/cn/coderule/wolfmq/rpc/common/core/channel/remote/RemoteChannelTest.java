package cn.coderule.wolfmq.rpc.common.core.channel.remote;

import cn.coderule.wolfmq.rpc.common.core.channel.ChannelProtocolType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteChannelTest {

    @Test
    void constructor_ShouldSetFields() {
        RemoteChannel channel = new RemoteChannel(
            "proxy-host", "192.168.1.1:10911", "192.168.1.2:10912",
            ChannelProtocolType.GRPC_V2, "attr1");
        assertEquals("proxy-host", channel.getRemoteProxyIp());
        assertEquals(ChannelProtocolType.GRPC_V2, channel.getType());
    }

    @Test
    void channelId_AsShortText_ShouldContainParts() {
        RemoteChannel.RemoteChannelId id = new RemoteChannel.RemoteChannelId(
            "proxy", "remote:1234", "local:5678", ChannelProtocolType.REMOTING);

        assertTrue(id.asShortText().contains("proxy"));
        assertTrue(id.asShortText().contains("remote:1234"));
        assertTrue(id.asShortText().contains("local:5678"));
    }

    @Test
    void channelId_AsLongText_ShouldBeSameAsShort() {
        RemoteChannel.RemoteChannelId id = new RemoteChannel.RemoteChannelId(
            "proxy", "remote:1234", "local:5678", ChannelProtocolType.REMOTING);

        assertEquals(id.asShortText(), id.asLongText());
    }

    @Test
    void channelId_CompareToSame_ShouldReturnZero() {
        RemoteChannel.RemoteChannelId id = new RemoteChannel.RemoteChannelId(
            "proxy", "remote:1234", "local:5678", ChannelProtocolType.REMOTING);

        assertEquals(0, id.compareTo(id));
    }
}