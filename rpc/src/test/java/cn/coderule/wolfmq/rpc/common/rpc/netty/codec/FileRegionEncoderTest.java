package cn.coderule.wolfmq.rpc.common.rpc.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.FileRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileRegionEncoderTest {

    @Test
    void allocateBuffer_ShouldReturnCompositeBuffer() throws Exception {
        FileRegionEncoder encoder = new FileRegionEncoder();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.alloc()).thenReturn(Unpooled.buffer().alloc());

        ByteBuf buf = encoder.allocateBuffer(ctx, mock(FileRegion.class), true);
        assertNotNull(buf);
        buf.release();
    }
}