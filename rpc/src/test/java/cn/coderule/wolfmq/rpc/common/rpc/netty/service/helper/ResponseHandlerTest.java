package cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.ResponseFuture;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseHandlerTest {

    private ResponseHandler handler;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadExecutor();
        handler = new ResponseHandler(executor);
    }

    @Test
    void start_ShouldNotThrow() throws Exception {
        handler.start();
    }

    @Test
    void shutdown_ShouldNotThrow() throws Exception {
        handler.start();
        handler.shutdown();
    }

    @Test
    void putResponse_ShouldStoreResponse() {
        ResponseFuture future = mock(ResponseFuture.class);
        handler.putResponse(1, future);
        assertNotNull(handler.getResponse(1));
    }

    @Test
    void removeResponse_ShouldRemoveResponse() {
        ResponseFuture future = mock(ResponseFuture.class);
        handler.putResponse(1, future);
        handler.removeResponse(1);
        assertNull(handler.getResponse(1));
    }

    @Test
    void getResponse_ForNonExistentKey_ShouldReturnNull() {
        assertNull(handler.getResponse(999));
    }

    @Test
    void scanResponseTable_ShouldRun() throws Exception {
        handler.start();
        Thread.sleep(200);
        handler.shutdown();
    }
}