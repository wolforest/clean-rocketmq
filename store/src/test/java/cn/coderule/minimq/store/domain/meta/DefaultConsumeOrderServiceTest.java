package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.test.ConfigMock;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultConsumeOrderServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void lockCommitAndUnlock_ShouldFollowOrderFlow() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        OrderLockCleaner cleaner = mock(OrderLockCleaner.class);
        DefaultConsumeOrderService service = new DefaultConsumeOrderService(
            storeConfig,
            tempDir.resolve("consume-order.json").toString(),
            cleaner
        );

        long now = System.currentTimeMillis();
        OrderRequest request = createRequest("topicA", "groupA", 0, "attempt-1", now, 60_000L, 10L);
        service.lock(request);

        OrderRequest anotherAttempt = createRequest("topicA", "groupA", 0, "attempt-2", now, 60_000L, 10L);
        assertTrue(service.isLocked(anotherAttempt));

        long nextOffset = service.commit(request);
        assertEquals(11L, nextOffset);

        service.unlock(request);
        assertFalse(service.isLocked(anotherAttempt));
    }

    @Test
    void load_WhenFileMissing_ShouldInitializeAndAllowOperations() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        String storePath = tempDir.resolve("missing-consume-order.json").toString();
        OrderLockCleaner cleaner = mock(OrderLockCleaner.class);

        DefaultConsumeOrderService service = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        assertDoesNotThrow(service::load);

        long now = System.currentTimeMillis();
        OrderRequest request = createRequest("topicB", "groupB", 1, "attempt-1", now, 60_000L, 20L);
        service.lock(request);
        service.updateInvisible(request);
        assertEquals(21L, service.commit(request));
    }

    private OrderRequest createRequest(
        String topic,
        String group,
        int queueId,
        String attemptId,
        long dequeueTime,
        long invisibleTime,
        long queueOffset
    ) {
        return OrderRequest.builder()
            .topicName(topic)
            .consumerGroup(group)
            .queueId(queueId)
            .attemptId(attemptId)
            .dequeueTime(dequeueTime)
            .invisibleTime(invisibleTime)
            .queueOffset(queueOffset)
            .offsetList(List.of(queueOffset))
            .build();
    }
}
