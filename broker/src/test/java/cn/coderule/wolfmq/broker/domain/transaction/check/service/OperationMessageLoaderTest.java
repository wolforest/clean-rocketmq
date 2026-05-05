package cn.coderule.wolfmq.broker.domain.transaction.check.service;

import cn.coderule.wolfmq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.wolfmq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OperationMessageLoaderTest {

    @Test
    void testLoadReturnsEmpty() {
        TransactionContext txnContext = mock(TransactionContext.class);
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        when(txnContext.getMessageService()).thenReturn(messageService);
        OperationMessageLoader loader = new OperationMessageLoader(txnContext);

        CheckContext checkContext = mock(CheckContext.class);
        MessageQueue operationQueue = mock(MessageQueue.class);
        when(checkContext.getOperationQueue()).thenReturn(operationQueue);
        when(checkContext.getOperationOffset()).thenReturn(0L);

        DequeueResult dequeueResult = mock(DequeueResult.class);
        when(dequeueResult.isEmpty()).thenReturn(true);
        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(dequeueResult);

        DequeueResult result = loader.load(checkContext);
        assertNotNull(result);
    }
}