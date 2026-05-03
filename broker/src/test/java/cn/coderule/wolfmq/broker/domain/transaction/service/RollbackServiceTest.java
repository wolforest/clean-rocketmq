package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.transaction.CommitResult;
import cn.coderule.wolfmq.domain.domain.transaction.SubmitRequest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RollbackServiceTest {

    @Test
    void testRollback() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        RollbackService rollbackService = new RollbackService(messageService);

        SubmitRequest request = mock(SubmitRequest.class);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageService.getMessage(request)).thenReturn(messageBO);

        CompletableFuture<CommitResult> future = rollbackService.rollback(request);
        CommitResult result = future.join();

        verify(messageService).deletePrepareMessage(request, messageBO);
        assertNotNull(result);
    }
}