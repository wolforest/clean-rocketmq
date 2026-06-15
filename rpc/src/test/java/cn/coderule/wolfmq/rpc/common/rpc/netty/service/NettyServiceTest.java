package cn.coderule.wolfmq.rpc.common.rpc.netty.service;

import cn.coderule.wolfmq.rpc.common.rpc.RpcProcessor;
import cn.coderule.wolfmq.rpc.common.rpc.netty.service.NettyService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettyServiceTest {

    private NettyService createNettyService() {
        return new NettyService(10, 20, 2) {
            @Override
            public void start() {}

            @Override
            public void shutdown() {}
        };
    }

    @Test
    void constructor_ShouldInitializeFields() {
        NettyService service = createNettyService();

        assertNotNull(service.getDispatcher());
        assertNotNull(service.getInvoker());
        assertNotNull(service.getCallbackExecutor());
        assertFalse(service.getStopping().get());
    }

    @Test
    void registerProcessor_WithCodeAndExecutor_ShouldNotThrow() {
        NettyService service = createNettyService();
        RpcProcessor processor = mock(RpcProcessor.class);
        when(processor.getCodeSet()).thenReturn(java.util.Set.of(100));
        ExecutorService executor = Executors.newSingleThreadExecutor();

        assertDoesNotThrow(() -> service.registerProcessor(100, processor, executor));
    }

    @Test
    void registerProcessor_WithNullExecutor_ShouldNotThrow() {
        NettyService service = createNettyService();
        RpcProcessor processor = mock(RpcProcessor.class);
        when(processor.getCodeSet()).thenReturn(java.util.Set.of(200));

        assertDoesNotThrow(() -> service.registerProcessor(200, processor, null));
    }

    @Test
    void registerProcessor_WithCodes_ShouldNotThrow() {
        NettyService service = createNettyService();
        RpcProcessor processor = mock(RpcProcessor.class);
        when(processor.getCodeSet()).thenReturn(java.util.Set.of(300, 301));
        ExecutorService executor = Executors.newSingleThreadExecutor();

        assertDoesNotThrow(() -> service.registerProcessor(java.util.Set.of(300, 301), processor, executor));
    }

    @Test
    void registerProcessor_WithEmptyCodeSet_ShouldNotRegister() {
        NettyService service = createNettyService();
        RpcProcessor processor = mock(RpcProcessor.class);
        when(processor.getCodeSet()).thenReturn(java.util.Set.of());

        assertDoesNotThrow(() -> service.registerProcessor(processor));
    }

    @Test
    void registerDefaultProcessor_ShouldNotThrow() {
        NettyService service = createNettyService();
        RpcProcessor processor = mock(RpcProcessor.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        assertDoesNotThrow(() -> service.registerDefaultProcessor(processor, executor));
    }
}