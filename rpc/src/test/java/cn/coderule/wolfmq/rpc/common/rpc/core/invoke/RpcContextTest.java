package cn.coderule.wolfmq.rpc.common.rpc.core.invoke;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RpcContextTest {

    @Test
    void testConstructorWithAddr() {
        String addr = "127.0.0.1:8080";
        RpcContext context = new RpcContext(addr);
        
        assertNotNull(context);
        assertEquals(addr, context.getAddr());
        assertNull(context.getChannelContext());
        assertNull(context.channel());
    }

    @Test
    void testConstructorWithChannelContext() {
        ChannelHandlerContext mockContext = mock(ChannelHandlerContext.class);
        Channel mockChannel = mock(Channel.class);
        when(mockContext.channel()).thenReturn(mockChannel);
        
        RpcContext context = new RpcContext(mockContext);
        
        assertNotNull(context);
        assertNull(context.getAddr());
        assertSame(mockContext, context.getChannelContext());
        assertSame(mockChannel, context.channel());
    }

    @Test
    void testChannelWithNullContext() {
        RpcContext context = new RpcContext("127.0.0.1:8080");
        
        Channel channel = context.channel();
        
        assertNull(channel);
    }

    @Test
    void testSettersAndGetters() {
        RpcContext context = new RpcContext("127.0.0.1:8080");
        
        ChannelHandlerContext mockContext = mock(ChannelHandlerContext.class);
        String newAddr = "192.168.1.1:9090";
        
        context.setChannelContext(mockContext);
        context.setAddr(newAddr);
        
        assertSame(mockContext, context.getChannelContext());
        assertEquals(newAddr, context.getAddr());
    }
}
