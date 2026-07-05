package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper.NettyDispatcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class NettyClientHandlerTest {

    @Test
    void constructor_ShouldCreateHandler() {
        NettyDispatcher dispatcher = new NettyDispatcher(Executors.newSingleThreadExecutor());
        NettyClientHandler handler = new NettyClientHandler(dispatcher);
        assertNotNull(handler);
    }
}