package cn.coderule.wolfmq.rpc.common.rpc.netty.codec;

import cn.coderule.wolfmq.rpc.common.rpc.core.enums.TlsMode;
import cn.coderule.wolfmq.rpc.common.rpc.netty.handler.TlsModeHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandshakeDecoderTest {

    @Test
    void handlerNames_ShouldBeCorrect() {
        assertEquals("handshakeHandler", HandshakeDecoder.HANDSHAKE_HANDLER_NAME);
        assertEquals("HAProxyDecoder", HandshakeDecoder.HA_PROXY_DECODER);
        assertEquals("HAProxyHandler", HandshakeDecoder.HA_PROXY_HANDLER);
    }

    @Test
    void constructor_ShouldCreate() {
        EventExecutorGroup executor = new DefaultEventExecutorGroup(1);
        TlsModeHandler tlsHandler = new TlsModeHandler(TlsMode.PERMISSIVE, null, executor);
        HandshakeDecoder decoder = new HandshakeDecoder(executor, tlsHandler);
        assertNotNull(decoder);
    }
}