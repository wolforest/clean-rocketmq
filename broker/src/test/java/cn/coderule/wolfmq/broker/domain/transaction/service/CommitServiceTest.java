package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.transaction.CommitResult;
import cn.coderule.wolfmq.domain.domain.transaction.SubmitRequest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitServiceTest {

    @Test
    void testCommitSuccess() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        CommitService commitService = new CommitService(messageService, messageFactory);

        SubmitRequest request = mock(SubmitRequest.class);
        MessageBO prepareMessage = mock(MessageBO.class);
        MessageBO commitMessage = mock(MessageBO.class);
        EnqueueResult enqueueResult = mock(EnqueueResult.class);

        when(messageService.getMessage(request)).thenReturn(prepareMessage);
        when(messageFactory.createCommitMessage(request, prepareMessage)).thenReturn(commitMessage);
        when(messageService.enqueueCommitMessage(request, commitMessage)).thenReturn(enqueueResult);
        when(enqueueResult.isSuccess()).thenReturn(true);

        CompletableFuture<CommitResult> future = commitService.commit(request);
        CommitResult result = future.join();

        assertEquals(1, result.getResponseCode());
        verify(messageService).deletePrepareMessage(request, prepareMessage);
    }

    @Test
    void testCommitFailure() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        CommitService commitService = new CommitService(messageService, messageFactory);

        SubmitRequest request = mock(SubmitRequest.class);
        MessageBO prepareMessage = mock(MessageBO.class);
        MessageBO commitMessage = mock(MessageBO.class);
        EnqueueResult enqueueResult = mock(EnqueueResult.class);

        when(messageService.getMessage(request)).thenReturn(prepareMessage);
        when(messageFactory.createCommitMessage(request, prepareMessage)).thenReturn(commitMessage);
        when(messageService.enqueueCommitMessage(request, commitMessage)).thenReturn(enqueueResult);
        when(enqueueResult.isSuccess()).thenReturn(false);

        CompletableFuture<CommitResult> future = commitService.commit(request);
        CommitResult result = future.join();

        assertEquals(-1, result.getResponseCode());
        verify(messageService, never()).deletePrepareMessage(any(), any());
    }
}