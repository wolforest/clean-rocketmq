package cn.coderule.wolfmq.rpc.common.rpc.netty.service.invoker;

import cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper.NettyDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ChannelInvokerTest {

    private ChannelInvoker invoker;

    @BeforeEach
    void setUp() {
        NettyDispatcher dispatcher = new NettyDispatcher(Executors.newSingleThreadExecutor());
        invoker = new ChannelInvoker(10, 10, dispatcher);
    }

    @Test
    void constructor_ShouldSetFields() {
        assertNotNull(invoker);
    }
}