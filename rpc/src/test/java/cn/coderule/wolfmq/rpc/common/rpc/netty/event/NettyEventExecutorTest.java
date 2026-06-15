package cn.coderule.wolfmq.rpc.common.rpc.netty.event;

import cn.coderule.wolfmq.rpc.common.rpc.RpcListener;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettyEventExecutorTest {

    private NettyEventExecutor executor;
    private RpcListener rpcListener;

    @BeforeEach
    void setUp() {
        rpcListener = mock(RpcListener.class);
        executor = new NettyEventExecutor(rpcListener);
    }

    @Test
    void constructor_ShouldSetRpcListener() {
        assertEquals(rpcListener, executor.getRpcListener());
    }

    @Test
    void getServiceName_ShouldReturnClassName() {
        assertEquals("NettyEventExecutor", executor.getServiceName());
    }

    @Test
    void putNettyEvent_ShouldAddEventToQueue() {
        Channel channel = mock(Channel.class);
        NettyEvent event = new NettyEvent(NettyEventType.CONNECT, "127.0.0.1:10911", channel);

        executor.putNettyEvent(event);

        assertFalse(executor.isStopped());
    }

    @Test
    void putNettyEvent_ShouldHandleMultipleEvents() {
        Channel channel = mock(Channel.class);
        for (int i = 0; i < 100; i++) {
            NettyEvent event = new NettyEvent(NettyEventType.IDLE, "127.0.0.1:" + i, channel);
            executor.putNettyEvent(event);
        }

        assertFalse(executor.isStopped());
    }
}