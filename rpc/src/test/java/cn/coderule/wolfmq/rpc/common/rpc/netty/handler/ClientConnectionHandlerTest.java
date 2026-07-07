package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.wolfmq.rpc.common.rpc.netty.event.NettyEventExecutor;
import cn.coderule.wolfmq.rpc.common.rpc.netty.event.NettyEventType;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientConnectionHandlerTest {

    private ClientConnectionHandler handler;
    private NettyEventExecutor eventExecutor;

    @BeforeEach
    void setUp() {
        NettyClient client = mock(NettyClient.class);
        eventExecutor = mock(NettyEventExecutor.class);
        handler = new ClientConnectionHandler(client, eventExecutor);
    }

    @Test
    void constructor_ShouldSetFields() {
        assertNotNull(handler);
    }

    @Test
    void channelActive_WithNullListener_ShouldNotThrow() throws Exception {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        when(eventExecutor.getRpcListener()).thenReturn(null);

        assertDoesNotThrow(() -> handler.channelActive(ctx));
        verify(eventExecutor, never()).putNettyEvent(any());
    }
}