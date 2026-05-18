package cn.coderule.wolfmq.domain.domain.transaction;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class CommitResultTest {

    @Test
    void testSuccess() {
        CommitResult result = CommitResult.success();
        assertEquals(1, result.getResponseCode());
    }

    @Test
    void testFailure() {
        CommitResult result = CommitResult.failure();
        assertEquals(-1, result.getResponseCode());
    }

    @Test
    void testSuccessFuture() {
        CompletableFuture<CommitResult> future = CommitResult.successFuture();
        CommitResult result = future.join();
        assertEquals(1, result.getResponseCode());
    }

    @Test
    void testFailureFuture() {
        CompletableFuture<CommitResult> future = CommitResult.failureFuture();
        CommitResult result = future.join();
        assertEquals(-1, result.getResponseCode());
    }

    @Test
    void testBuilder() {
        MessageBO msg = new MessageBO();
        CommitResult result = CommitResult.builder()
            .responseCode(0)
            .responseMessage("ok")
            .messageBO(msg)
            .build();

        assertEquals(0, result.getResponseCode());
        assertEquals("ok", result.getResponseMessage());
        assertEquals(msg, result.getMessageBO());
    }

    @Test
    void testNoArgsConstructor() {
        CommitResult result = new CommitResult();
        assertNotNull(result);
    }
}