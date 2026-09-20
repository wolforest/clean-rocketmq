package cn.coderule.wolfmq.rpc.common.rpc.netty.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NettyDecoderTest {

    @Test
    void decoder_ShouldBeInstantiable() {
        NettyDecoder decoder = new NettyDecoder();
        assertNotNull(decoder);
    }
}