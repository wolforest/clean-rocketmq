package cn.coderule.wolfmq.rpc.common.rpc.netty.event;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettyEventTest {

    @Test
    void constructor_ShouldSetFields() {
        Channel channel = mock(Channel.class);
        NettyEvent event = new NettyEvent(NettyEventType.CONNECT, "127.0.0.1:10911", channel);

        assertEquals(NettyEventType.CONNECT, event.getType());
        assertEquals("127.0.0.1:10911", event.getAddress());
        assertEquals(channel, event.getChannel());
    }

    @Test
    void toString_ShouldContainTypeAndAddress() {
        Channel channel = mock(Channel.class);
        NettyEvent event = new NettyEvent(NettyEventType.IDLE, "192.168.1.1:8080", channel);

        String str = event.toString();
        assertTrue(str.contains("IDLE"));
        assertTrue(str.contains("192.168.1.1:8080"));
    }

    @Test
    void allEventTypes_ShouldBeDefined() {
        NettyEventType[] types = NettyEventType.values();
        assertEquals(5, types.length);
        assertNotNull(NettyEventType.valueOf("CONNECT"));
        assertNotNull(NettyEventType.valueOf("CLOSE"));
        assertNotNull(NettyEventType.valueOf("IDLE"));
        assertNotNull(NettyEventType.valueOf("EXCEPTION"));
        assertNotNull(NettyEventType.valueOf("ACTIVE"));
    }
}