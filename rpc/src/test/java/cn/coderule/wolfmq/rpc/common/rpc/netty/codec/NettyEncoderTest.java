package cn.coderule.wolfmq.rpc.common.rpc.netty.codec;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettyEncoderTest {

    @Test
    void encoder_ShouldBeInstantiable() {
        NettyEncoder encoder = new NettyEncoder();
        assertNotNull(encoder);
    }

    @Test
    void encoder_ShouldImplementSharable() {
        NettyEncoder encoder = new NettyEncoder();
        assertTrue(encoder.isSharable());
    }
}