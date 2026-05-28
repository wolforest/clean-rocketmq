package cn.coderule.wolfmq.rpc.broker.grpc;

import cn.coderule.wolfmq.rpc.common.core.relay.response.Result;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ResultFutureTest {

    @Test
    void testConstructor_initializesFields() {
        CompletableFuture<Result<String>> future = new CompletableFuture<>();
        ResultFuture<String> resultFuture = new ResultFuture<>(future);

        assertNotNull(resultFuture.getFuture());
        assertNotNull(resultFuture.getCreateTime());
        assertTrue(resultFuture.getCreateTime() > 0);
    }

    @Test
    void testConstructor_setsCurrentTime() {
        long before = System.currentTimeMillis();
        CompletableFuture<Result<String>> future = new CompletableFuture<>();
        ResultFuture<String> resultFuture = new ResultFuture<>(future);
        long after = System.currentTimeMillis();

        assertTrue(resultFuture.getCreateTime() >= before);
        assertTrue(resultFuture.getCreateTime() <= after);
    }

    @Test
    void testGetFuture_returnsProvidedFuture() {
        CompletableFuture<Result<String>> future = new CompletableFuture<>();
        ResultFuture<String> resultFuture = new ResultFuture<>(future);

        assertEquals(future, resultFuture.getFuture());
    }
}