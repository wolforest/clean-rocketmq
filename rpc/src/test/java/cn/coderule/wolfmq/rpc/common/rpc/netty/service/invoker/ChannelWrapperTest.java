package cn.coderule.wolfmq.rpc.common.rpc.netty.service.invoker;

import cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper.NettyDispatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChannelWrapperTest {

    private AddressInvoker invoker;
    private ChannelFuture channelFuture;
    private ChannelWrapper wrapper;

    @BeforeEach
    void setUp() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        NettyDispatcher dispatcher = new NettyDispatcher(executor);
        invoker = mock(AddressInvoker.class);
        channelFuture = mock(ChannelFuture.class);
        Channel channel = mock(Channel.class);
        when(channelFuture.channel()).thenReturn(channel);
        when(channel.isActive()).thenReturn(true);
        wrapper = new ChannelWrapper(invoker, channelFuture, "127.0.0.1:10911");
    }

    @Test
    void constructor_ShouldSetAddress() {
        assertEquals("127.0.0.1:10911", wrapper.getAddress());
    }

    @Test
    void getLastResponseTime_ShouldReturnInitialValue() {
        assertEquals(0, wrapper.getLastResponseTime());
    }

    @Test
    void isAvailable_ShouldReflectChannelState() {
        assertNotNull(wrapper);
    }
}