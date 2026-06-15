package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestCodeCounterTest {

    private RequestCodeCounter counter;

    @BeforeEach
    void setUp() {
        counter = new RequestCodeCounter();
    }

    @Test
    void constructor_ShouldInitialize() {
        assertNotNull(counter);
    }

    @Test
    void channelRead_WithRpcCommand_ShouldCountInbound() {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        RpcCommand cmd = RpcCommand.createRequestCommand(100);
        cmd.setBody(new byte[0]);

        counter.channelRead(ctx, cmd);

        String snapshot = counter.getInBoundSnapshotString();
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("100"));
    }

    @Test
    void channelRead_WithNonRpcCommand_ShouldPassThrough() {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        String msg = "not a command";

        counter.channelRead(ctx, msg);

        assertNull(counter.getInBoundSnapshotString());
        verify(ctx).fireChannelRead(msg);
    }

    @Test
    void write_WithRpcCommand_ShouldCountOutbound() throws Exception {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ChannelPromise promise = mock(ChannelPromise.class);
        RpcCommand cmd = RpcCommand.createResponseCommand(200, "OK");
        cmd.setBody(new byte[0]);

        counter.write(ctx, cmd, promise);

        String snapshot = counter.getOutBoundSnapshotString();
        assertNotNull(snapshot);
    }

    @Test
    void getInBoundSnapshotString_ShouldReturnNullWhenEmpty() {
        assertNull(counter.getInBoundSnapshotString());
    }

    @Test
    void getOutBoundSnapshotString_ShouldReturnNullWhenEmpty() {
        assertNull(counter.getOutBoundSnapshotString());
    }
}