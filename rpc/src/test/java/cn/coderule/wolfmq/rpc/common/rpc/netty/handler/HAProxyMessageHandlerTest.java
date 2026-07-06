package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HAProxyMessageHandlerTest {

    @Test
    void constructor_ShouldCreateHandler() {
        HAProxyMessageHandler handler = new HAProxyMessageHandler();
        assertNotNull(handler);
    }
}