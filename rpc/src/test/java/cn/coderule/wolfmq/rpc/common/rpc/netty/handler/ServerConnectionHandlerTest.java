package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import cn.coderule.wolfmq.rpc.common.rpc.netty.event.NettyEventExecutor;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerConnectionHandlerTest {

    private ServerConnectionHandler handler;
    private NettyEventExecutor eventExecutor;

    @BeforeEach
    void setUp() {
        eventExecutor = mock(NettyEventExecutor.class);
        handler = new ServerConnectionHandler(eventExecutor);
    }

    @Test
    void constructor_ShouldSetFields() {
        assertNotNull(handler);
    }

    @Test
    void channelRegistered_WithNullListener_ShouldNotThrow() throws Exception {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        when(eventExecutor.getRpcListener()).thenReturn(null);

        assertDoesNotThrow(() -> handler.channelRegistered(ctx));
        verify(eventExecutor, never()).putNettyEvent(any());
    }

    @Test
    void channelUnregistered_WithNullListener_ShouldNotThrow() throws Exception {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        when(eventExecutor.getRpcListener()).thenReturn(null);

        assertDoesNotThrow(() -> handler.channelUnregistered(ctx));
        verify(eventExecutor, never()).putNettyEvent(any());
    }
}