package com.wolfmq.store.domain.mq.queue;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.core.mmap.InsertStatus;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.mq.dto.EnqueueFuture;
import com.wolfmq.common.domain.mq.dto.EnqueueResult;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.testing.MessageMock;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EnqueueServiceTest {
    protected ApplicationContext context = new ApplicationContext();


    @Test
    void enqueueAsyncAssignsAndIncreasesOffsetWhenInsertSuccess() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAssignConsumeOffset(true);
        CommitLogFacade commitLog = mock(CommitLogFacade.class);
        ConsumeQueueFacade consumeQueueFacade = mock(ConsumeQueueFacade.class);
        CommitLogSynchronizer synchronizer = mock(CommitLogSynchronizer.class);

        EnqueueService service = new EnqueueService(storeConfig, commitLog, consumeQueueFacade);
        service.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("TOPIC_A", 0, 0);
        when(consumeQueueFacade.assignOffset("TOPIC_A", 0)).thenReturn(5L);

        InsertResult insertResult = new InsertResult(InsertStatus.PUT_OK, 0, 1, 0);
        EnqueueFuture future = EnqueueFuture.success(insertResult, message);
        when(commitLog.insert(message)).thenReturn(future);

        CompletableFuture<EnqueueResult> resultFuture = CompletableFuture.completedFuture(EnqueueResult.success(insertResult, message));
        when(synchronizer.sync(future)).thenReturn(resultFuture);

        service.enqueueAsync(message);

        assertEquals(5L, message.getQueueOffset());
        verify(consumeQueueFacade).assignOffset("TOPIC_A", 0);
        verify(consumeQueueFacade).increaseOffset("TOPIC_A", 0);
        verify(synchronizer).sync(future);
    }

    @Test
    void enqueueAsyncSkipsOffsetAssignmentWhenDisabled() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAssignConsumeOffset(false);

        CommitLogFacade commitLog = mock(CommitLogFacade.class);
        ConsumeQueueFacade consumeQueueFacade = mock(ConsumeQueueFacade.class);
        CommitLogSynchronizer synchronizer = mock(CommitLogSynchronizer.class);

        EnqueueService service = new EnqueueService(storeConfig, commitLog, consumeQueueFacade);
        service.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("TOPIC_B", 1, 0);

        EnqueueFuture future = EnqueueFuture.failure();
        when(commitLog.insert(message)).thenReturn(future);
        when(synchronizer.sync(future)).thenReturn(CompletableFuture.completedFuture(EnqueueResult.failure()));

        service.enqueueAsync(message);

        assertEquals(-1, message.getQueueOffset());
        verifyNoInteractions(consumeQueueFacade);
        verify(synchronizer).sync(future);
    }
}
