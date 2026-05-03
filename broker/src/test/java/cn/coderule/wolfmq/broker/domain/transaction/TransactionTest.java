package cn.coderule.wolfmq.broker.domain.transaction;

import cn.coderule.wolfmq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.wolfmq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.wolfmq.broker.domain.transaction.service.CommitService;
import cn.coderule.wolfmq.broker.domain.transaction.service.PrepareService;
import cn.coderule.wolfmq.broker.domain.transaction.service.RollbackService;
import cn.coderule.wolfmq.broker.domain.transaction.service.SubscribeService;
import cn.coderule.wolfmq.domain.core.enums.TransactionType;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.transaction.CommitResult;
import cn.coderule.wolfmq.domain.domain.transaction.SubmitRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionTest {

    private ReceiptRegistry receiptRegistry;
    private SubscribeService subscribeService;
    private PrepareService prepareService;
    private CommitService commitService;
    private RollbackService rollbackService;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        receiptRegistry = mock(ReceiptRegistry.class);
        subscribeService = mock(SubscribeService.class);
        prepareService = mock(PrepareService.class);
        commitService = mock(CommitService.class);
        rollbackService = mock(RollbackService.class);
        transaction = new Transaction(receiptRegistry, subscribeService, prepareService, commitService, rollbackService);
    }

    @Test
    void testSubscribe() {
        RequestContext context = mock(RequestContext.class);
        transaction.subscribe(context, "topic", "group");
        verify(subscribeService).subscribe(context, "topic", "group");
    }

    @Test
    void testPrepare() {
        RequestContext context = mock(RequestContext.class);
        MessageBO messageBO = mock(MessageBO.class);
        CompletableFuture<EnqueueResult> future = CompletableFuture.completedFuture(mock(EnqueueResult.class));
        when(prepareService.prepare(context, messageBO)).thenReturn(future);

        CompletableFuture<EnqueueResult> result = transaction.prepare(context, messageBO);
        assertNotNull(result);
        verify(prepareService).prepare(context, messageBO);
    }

    @Test
    void testSubmitCommit() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getTransactionType()).thenReturn(TransactionType.COMMIT);
        when(request.getProducerGroup()).thenReturn("group");
        when(request.getTransactionId()).thenReturn("txn123");

        Receipt receipt = mock(Receipt.class);
        when(receipt.getStoreGroup()).thenReturn("store1");
        when(receipt.getMessageId()).thenReturn("msg1");
        when(receipt.getCommitOffset()).thenReturn(100L);
        when(receipt.getQueueOffset()).thenReturn(5L);
        when(receiptRegistry.poll("group", "txn123")).thenReturn(receipt);

        CompletableFuture<CommitResult> future = CompletableFuture.completedFuture(mock(CommitResult.class));
        when(commitService.commit(request)).thenReturn(future);

        CompletableFuture<CommitResult> result = transaction.submit(request);
        assertNotNull(result);
        verify(request).setStoreGroup("store1");
        verify(request).setMessageId("msg1");
        verify(request).setCommitOffset(100L);
        verify(request).setQueueOffset(5L);
    }

    @Test
    void testSubmitRollback() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getTransactionType()).thenReturn(TransactionType.ROLLBACK);
        when(request.getProducerGroup()).thenReturn("group");
        when(request.getTransactionId()).thenReturn("txn123");

        Receipt receipt = mock(Receipt.class);
        when(receipt.getStoreGroup()).thenReturn("store1");
        when(receipt.getMessageId()).thenReturn("msg1");
        when(receipt.getCommitOffset()).thenReturn(100L);
        when(receipt.getQueueOffset()).thenReturn(5L);
        when(receiptRegistry.poll("group", "txn123")).thenReturn(receipt);

        CompletableFuture<CommitResult> future = CompletableFuture.completedFuture(mock(CommitResult.class));
        when(rollbackService.rollback(request)).thenReturn(future);

        CompletableFuture<CommitResult> result = transaction.submit(request);
        assertNotNull(result);
        verify(rollbackService).rollback(request);
    }

    @Test
    void testSubmitNoReceipt() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getTransactionType()).thenReturn(TransactionType.COMMIT);
        when(request.getProducerGroup()).thenReturn("group");
        when(request.getTransactionId()).thenReturn("txn123");
        when(receiptRegistry.poll("group", "txn123")).thenReturn(null);

        assertThrows(InvalidRequestException.class, () -> transaction.submit(request));
    }
}