package cn.coderule.wolfmq.store.domain.commitlog.vo;

import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class GroupCommitRequestTest {

    @Test
    void testDefaultConstructor() {
        GroupCommitRequest request = new GroupCommitRequest();

        assertNotNull(request);
        assertEquals(0L, request.getOffset());
        assertEquals(0L, request.getNextOffset());
        assertNotNull(request.getFlushOKFuture());
        assertEquals(1, request.getAckNums());
        assertEquals(0L, request.getDeadLine());
    }

    @Test
    void testBuilder() {
        CompletableFuture<EnqueueStatus> future = new CompletableFuture<>();

        GroupCommitRequest request = GroupCommitRequest.builder()
            .offset(100L)
            .nextOffset(200L)
            .flushOKFuture(future)
            .ackNums(3)
            .deadLine(System.currentTimeMillis() + 5000)
            .build();

        assertEquals(100L, request.getOffset());
        assertEquals(200L, request.getNextOffset());
        assertSame(future, request.getFlushOKFuture());
        assertEquals(3, request.getAckNums());
        assertTrue(request.getDeadLine() > 0);
    }

    @Test
    void testWakeupCompletesFuture() {
        GroupCommitRequest request = new GroupCommitRequest();

        assertFalse(request.getFlushOKFuture().isDone());

        request.wakeup(EnqueueStatus.PUT_OK);

        assertTrue(request.getFlushOKFuture().isDone());
    }

    @Test
    void testWakeupWithSuccess() throws Exception {
        GroupCommitRequest request = new GroupCommitRequest();

        request.wakeup(EnqueueStatus.PUT_OK);

        EnqueueStatus result = request.future().get(1, TimeUnit.SECONDS);
        assertEquals(EnqueueStatus.PUT_OK, result);
    }

    @Test
    void testWakeupWithFailure() throws Exception {
        GroupCommitRequest request = new GroupCommitRequest();

        request.wakeup(EnqueueStatus.FLUSH_DISK_TIMEOUT);

        EnqueueStatus result = request.future().get(1, TimeUnit.SECONDS);
        assertEquals(EnqueueStatus.FLUSH_DISK_TIMEOUT, result);
    }

    @Test
    void testFutureReturnsFuture() {
        GroupCommitRequest request = new GroupCommitRequest();

        CompletableFuture<EnqueueStatus> future = request.future();

        assertNotNull(future);
        assertSame(request.getFlushOKFuture(), future);
    }

    @Test
    void testSettersAndGetters() {
        GroupCommitRequest request = new GroupCommitRequest();

        request.setOffset(500L);
        request.setNextOffset(600L);
        request.setAckNums(2);
        request.setDeadLine(1234567890L);

        assertEquals(500L, request.getOffset());
        assertEquals(600L, request.getNextOffset());
        assertEquals(2, request.getAckNums());
        assertEquals(1234567890L, request.getDeadLine());
    }

}
