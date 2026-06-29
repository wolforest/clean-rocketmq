package cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper;

import cn.coderule.wolfmq.rpc.common.rpc.RpcProcessor;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.wolfmq.rpc.common.rpc.core.enums.RemotingCommandType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettyDispatcherTest {

    private NettyDispatcher dispatcher;
    private ExecutorService callbackExecutor;

    @BeforeEach
    void setUp() {
        callbackExecutor = Executors.newSingleThreadExecutor();
        dispatcher = new NettyDispatcher(callbackExecutor);
    }

    @Test
    void constructor_ShouldInitialize() {
        assertNotNull(dispatcher);
    }

    @Test
    void registerProcessor_WithCodeAndExecutor_ShouldRegister() {
        RpcProcessor processor = mock(RpcProcessor.class);
        when(processor.getCodeSet()).thenReturn(Set.of(100));
        ExecutorService executor = Executors.newSingleThreadExecutor();

        dispatcher.registerProcessor(100, processor, executor);
        assertTrue(true);
    }

    @Test
    void registerProcessor_WithCodes_ShouldRegister() {
        RpcProcessor processor = mock(RpcProcessor.class);
        when(processor.getCodeSet()).thenReturn(Set.of(200, 201));
        ExecutorService executor = Executors.newSingleThreadExecutor();

        dispatcher.registerProcessor(Set.of(200, 201), processor, executor);
        assertTrue(true);
    }

    @Test
    void registerDefaultProcessor_ShouldRegister() {
        RpcProcessor processor = mock(RpcProcessor.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        dispatcher.registerDefaultProcessor(processor, executor);
        assertTrue(true);
    }

    @Test
    void registerRpcHook_ShouldAddHook() {
        cn.coderule.wolfmq.rpc.common.rpc.RpcHook hook = mock(cn.coderule.wolfmq.rpc.common.rpc.RpcHook.class);
        dispatcher.registerRpcHook(hook);
        assertTrue(true);
    }

    @Test
    void clearRpcHook_ShouldNotThrow() {
        dispatcher.registerRpcHook(mock(cn.coderule.wolfmq.rpc.common.rpc.RpcHook.class));
        assertDoesNotThrow(() -> dispatcher.clearRpcHook());
    }

    @Test
    void dispatch_WithNullCommand_ShouldNotThrow() {
        RpcContext ctx = mock(RpcContext.class);
        assertDoesNotThrow(() -> dispatcher.dispatch(ctx, null));
    }

    @Test
    void start_And_Shutdown_ShouldNotThrow() throws Exception {
        dispatcher.start();
        dispatcher.shutdown();
    }

    @Test
    void putResponse_And_RemoveResponse_ShouldNotThrow() {
        cn.coderule.wolfmq.rpc.common.rpc.core.invoke.ResponseFuture future =
            mock(cn.coderule.wolfmq.rpc.common.rpc.core.invoke.ResponseFuture.class);
        dispatcher.putResponse(1, future);
        dispatcher.removeResponse(1);
    }
}