package cn.coderule.wolfmq.store.domain.mq.queue;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.enums.store.InsertStatus;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.test.MessageMock;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EnqueueServiceTest {

    @Test
    void enqueueAsyncAssignsAndIncreasesOffsetWhenInsertSuccess() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAssignConsumeOffset(true);
        CommitLogManager commitLog = mock(CommitLogManager.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        CommitLogSynchronizer synchronizer = mock(CommitLogSynchronizer.class);

        EnqueueService service = new EnqueueService(storeConfig, commitLog, consumeQueueManager);
        service.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("TOPIC_A", 0, 0);
        when(consumeQueueManager.assignOffset("TOPIC_A", 0)).thenReturn(5L);

        InsertResult insertResult = new InsertResult(InsertStatus.PUT_OK, 0, 1, 0);
        EnqueueFuture future = EnqueueFuture.success(insertResult, message);
        when(commitLog.insert(message)).thenReturn(future);

        CompletableFuture<EnqueueResult> resultFuture = CompletableFuture.completedFuture(EnqueueResult.success(insertResult, message));
        when(synchronizer.sync(future)).thenReturn(resultFuture);

        service.enqueueAsync(message);

        assertEquals(5L, message.getQueueOffset());
        verify(consumeQueueManager).assignOffset("TOPIC_A", 0);
        verify(consumeQueueManager).increaseOffset("TOPIC_A", 0);
        verify(synchronizer).sync(future);
    }

    @Test
    void enqueueAsyncSkipsOffsetAssignmentWhenDisabled() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAssignConsumeOffset(false);

        CommitLogManager commitLog = mock(CommitLogManager.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        CommitLogSynchronizer synchronizer = mock(CommitLogSynchronizer.class);

        EnqueueService service = new EnqueueService(storeConfig, commitLog, consumeQueueManager);
        service.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("TOPIC_B", 1, 0);

        EnqueueFuture future = EnqueueFuture.failure();
        when(commitLog.insert(message)).thenReturn(future);
        when(synchronizer.sync(future)).thenReturn(CompletableFuture.completedFuture(EnqueueResult.failure()));

        service.enqueueAsync(message);

        assertEquals(-1, message.getQueueOffset());
        verifyNoInteractions(consumeQueueManager);
        verify(synchronizer).sync(future);
    }
}
