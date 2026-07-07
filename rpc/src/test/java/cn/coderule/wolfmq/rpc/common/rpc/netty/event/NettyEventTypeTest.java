package cn.coderule.wolfmq.rpc.common.rpc.netty.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NettyEventTypeTest {

    @Test
    void allEventTypes_ShouldBeDefined() {
        NettyEventType[] types = NettyEventType.values();
        assertEquals(5, types.length);
    }

    @Test
    void valueOf_ShouldReturnCorrectType() {
        assertEquals(NettyEventType.CONNECT, NettyEventType.valueOf("CONNECT"));
        assertEquals(NettyEventType.CLOSE, NettyEventType.valueOf("CLOSE"));
        assertEquals(NettyEventType.IDLE, NettyEventType.valueOf("IDLE"));
        assertEquals(NettyEventType.EXCEPTION, NettyEventType.valueOf("EXCEPTION"));
        assertEquals(NettyEventType.ACTIVE, NettyEventType.valueOf("ACTIVE"));
    }
}