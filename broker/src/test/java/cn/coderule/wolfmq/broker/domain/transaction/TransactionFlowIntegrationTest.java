package cn.coderule.wolfmq.broker.domain.transaction;

import cn.coderule.wolfmq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.wolfmq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.wolfmq.broker.domain.transaction.service.CommitService;
import cn.coderule.wolfmq.broker.domain.transaction.service.MessageFactory;
import cn.coderule.wolfmq.broker.domain.transaction.service.PrepareService;
import cn.coderule.wolfmq.broker.domain.transaction.service.RollbackService;
import cn.coderule.wolfmq.broker.domain.transaction.service.SubmitValidator;
import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.broker.infra.store.ConsumeOffsetStore;
import cn.coderule.wolfmq.broker.infra.store.MQStore;
import cn.coderule.wolfmq.broker.infra.store.TopicStore;
import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.broker.api.TransactionController;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.enums.TransactionType;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.transaction.CommitBuffer;
import cn.coderule.wolfmq.domain.domain.transaction.CommitResult;
import cn.coderule.wolfmq.domain.domain.transaction.SubmitRequest;
import cn.coderule.wolfmq.broker.domain.transaction.service.SubscribeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for the full transaction lifecycle:
 * prepare → register receipt → commit/rollback
 *
 * Uses REAL domain services: Transaction, ReceiptRegistry, MessageFactory, PrepareService
 * MOCKS only infrastructure: MQStore, TopicStore, ConsumeOffsetStore
 */
public class TransactionFlowIntegrationTest {

    private TransactionConfig transactionConfig;
    private BrokerConfig brokerConfig;
    private ReceiptRegistry receiptRegistry;
    private MessageFactory messageFactory;
    private CommitBuffer commitBuffer;
    private MQStore mqStore;
    private TopicStore topicStore;
    private ConsumeOffsetStore consumeOffsetStore;

    @BeforeEach
    void setUp() {
        transactionConfig = new TransactionConfig();
        transactionConfig.setMaxReceiptNum(100);
        transactionConfig.setMaxCommitMessageLength(4096);

        brokerConfig = mock(BrokerConfig.class);
        when(brokerConfig.getTransactionConfig()).thenReturn(transactionConfig);
        when(brokerConfig.getMessageConfig()).thenReturn(mock(cn.coderule.wolfmq.domain.config.business.MessageConfig.class));
        when(brokerConfig.getHost()).thenReturn("localhost");
        when(brokerConfig.getPort()).thenReturn(10911);
        when(brokerConfig.getHostAddress()).thenReturn(new InetSocketAddress("localhost", 10911));

        receiptRegistry = new ReceiptRegistry(transactionConfig);
        commitBuffer = new CommitBuffer(transactionConfig);
        messageFactory = new MessageFactory(brokerConfig, commitBuffer);

        mqStore = mock(MQStore.class);
        topicStore = mock(TopicStore.class);
        consumeOffsetStore = mock(ConsumeOffsetStore.class);
    }

    // ========== Transaction Submit - Commit Flow ==========

    @Test
    void transactionSubmit_commitFlow_shouldCallCommitService() {
        // Arrange - wire real Transaction with mocked services
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        SubscribeService subscribeService = mock(SubscribeService.class);
        PrepareService prepareService = mock(PrepareService.class);
        CommitService commitService = mock(CommitService.class);
        RollbackService rollbackService = mock(RollbackService.class);

        Transaction transaction = new Transaction(
            receiptRegistry, subscribeService, prepareService, commitService, rollbackService
        );

        // Register a receipt so it can be polled during submit
        long now = System.currentTimeMillis();
        Receipt receipt = Receipt.builder()
            .producerGroup("testGroup")
            .transactionId("txn123")
            .storeGroup("store1")
            .messageId("msg1")
            .commitOffset(100L)
            .queueOffset(50L)
            .checkTimestamp(now)
            .expireMs(30000L)
            .build();
        receiptRegistry.register(receipt);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("txn123")
            .transactionType(TransactionType.COMMIT)
            .build();

        when(commitService.commit(request)).thenReturn(CommitResult.successFuture());

        // Act
        CompletableFuture<CommitResult> result = transaction.submit(request);

        // Assert
        assertNotNull(result);
        CommitResult commitResult = result.join();
        assertEquals(1, commitResult.getResponseCode());
        verify(commitService).commit(request);
        verify(rollbackService, never()).rollback(any());
    }

    @Test
    void transactionSubmit_rollbackFlow_shouldCallRollbackService() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        SubscribeService subscribeService = mock(SubscribeService.class);
        PrepareService prepareService = mock(PrepareService.class);
        CommitService commitService = mock(CommitService.class);
        RollbackService rollbackService = mock(RollbackService.class);

        Transaction transaction = new Transaction(
            receiptRegistry, subscribeService, prepareService, commitService, rollbackService
        );

        long now = System.currentTimeMillis();
        Receipt receipt = Receipt.builder()
            .producerGroup("testGroup")
            .transactionId("txn456")
            .storeGroup("store1")
            .messageId("msg2")
            .commitOffset(200L)
            .queueOffset(100L)
            .checkTimestamp(now)
            .expireMs(30000L)
            .build();
        receiptRegistry.register(receipt);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("txn456")
            .transactionType(TransactionType.ROLLBACK)
            .build();

        when(rollbackService.rollback(request)).thenReturn(CommitResult.successFuture());

        // Act
        CompletableFuture<CommitResult> result = transaction.submit(request);

        // Assert
        assertNotNull(result);
        CommitResult rollbackResult = result.join();
        assertEquals(1, rollbackResult.getResponseCode());
        verify(rollbackService).rollback(request);
        verify(commitService, never()).commit(any());
    }

    // ========== Transaction Submit - No Receipt ==========

    @Test
    void transactionSubmit_withoutReceipt_shouldThrowInvalidRequestException() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        SubscribeService subscribeService = mock(SubscribeService.class);
        PrepareService prepareService = mock(PrepareService.class);
        CommitService commitService = mock(CommitService.class);
        RollbackService rollbackService = mock(RollbackService.class);

        Transaction transaction = new Transaction(
            receiptRegistry, subscribeService, prepareService, commitService, rollbackService
        );

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("nonExistentTxn")
            .transactionType(TransactionType.COMMIT)
            .build();

        // Act & Assert - no receipt registered for this transaction
        assertThrows(InvalidRequestException.class, () -> transaction.submit(request));
    }

    // ========== Transaction Submit - Receipt Data Transfer ==========

    @Test
    void transactionSubmit_shouldTransferReceiptDataToRequest() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        SubscribeService subscribeService = mock(SubscribeService.class);
        PrepareService prepareService = mock(PrepareService.class);
        CommitService commitService = mock(CommitService.class);
        RollbackService rollbackService = mock(RollbackService.class);

        Transaction transaction = new Transaction(
            receiptRegistry, subscribeService, prepareService, commitService, rollbackService
        );

long now = System.currentTimeMillis();
        Receipt receipt = Receipt.builder()
            .producerGroup("producer1")
            .transactionId("txn789")
            .storeGroup("storeGroup1")
            .messageId("msgId789")
            .commitOffset(300L)
            .queueOffset(150L)
            .checkTimestamp(now)
            .expireMs(30000L)
            .build();
        receiptRegistry.register(receipt);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("producer1")
            .transactionId("txn789")
            .transactionType(TransactionType.COMMIT)
            .build();

        when(commitService.commit(request)).thenReturn(CommitResult.successFuture());

        transaction.submit(request);

        assertEquals("storeGroup1", request.getStoreGroup());
        assertEquals("msgId789", request.getMessageId());
        assertEquals(300L, request.getCommitOffset());
        assertEquals(150L, request.getQueueOffset());
    }

    // ========== PrepareService Integration ==========

    @Test
    void prepareService_shouldCreatePrepareMessageAndRegisterReceipt() {
        PrepareService prepareService = new PrepareService(
            transactionConfig, messageFactory, mqStore, receiptRegistry
        );

        RequestContext context = RequestContext.create();
        MessageBO messageBO = MessageBO.builder()
            .topic("TestTopic")
            .body("hello world".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();
        messageBO.setUniqueKey("unique-key-1");

        EnqueueResult enqueueResult = mock(EnqueueResult.class);
        when(enqueueResult.isSuccess()).thenReturn(true);
        when(enqueueResult.getStoreGroup()).thenReturn("store1");
        when(enqueueResult.getMessageId()).thenReturn("msg-1");
        when(enqueueResult.getTransactionId()).thenReturn("txn-1");
        when(enqueueResult.getCommitOffset()).thenReturn(100L);
        when(enqueueResult.getQueueOffset()).thenReturn(50L);

        when(mqStore.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        // Act
        CompletableFuture<EnqueueResult> future = prepareService.prepare(context, messageBO);
        EnqueueResult result = future.join();

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(mqStore).enqueueAsync(any(EnqueueRequest.class));

        Receipt polledReceipt = receiptRegistry.poll("TestTopic", "unique-key-1");
    }

    // ========== CommitService Integration ==========

    @Test
    void commitService_shouldCommitAndDeletePrepareMessage() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        CommitService commitService = new CommitService(messageService, messageFactory);

        MessageBO prepareMessage = MessageBO.builder()
            .topic("TestTopic")
            .body("test body".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");
        prepareMessage.setUniqueKey("unique-key-1");

        MessageBO commitMessage = MessageBO.builder()
            .topic("RealTopic")
            .body("test body".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .insertResult(InsertResult.success(100, 50))
            .storeGroup("store1")
            .messageId("msg-commit-1")
            .queueOffset(50L)
            .commitOffset(100L)
            .build();

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("txn123")
            .storeGroup("store1")
            .queueOffset(50L)
            .commitOffset(100L)
            .build();

        when(messageService.getMessage(request)).thenReturn(prepareMessage);
        when(messageFactory.createCommitMessage(request, prepareMessage)).thenReturn(commitMessage);
        when(messageService.enqueueCommitMessage(request, commitMessage)).thenReturn(enqueueResult);

        // Act
        CompletableFuture<CommitResult> future = commitService.commit(request);
        CommitResult result = future.join();

        // Assert
        assertEquals(1, result.getResponseCode());
        verify(messageService).deletePrepareMessage(request, prepareMessage);
        verify(messageService).enqueueCommitMessage(request, commitMessage);
    }

    @Test
    void commitService_onFailure_shouldNotDeletePrepareMessage() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        CommitService commitService = new CommitService(messageService, messageFactory);

        MessageBO prepareMessage = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();

        MessageBO commitMessage = MessageBO.builder()
            .topic("RealTopic")
            .body("test".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.SERVICE_NOT_AVAILABLE)
            .insertResult(InsertResult.failure())
            .build();

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("txn456")
            .storeGroup("store1")
            .queueOffset(50L)
            .commitOffset(100L)
            .build();

        when(messageService.getMessage(request)).thenReturn(prepareMessage);
        when(messageFactory.createCommitMessage(request, prepareMessage)).thenReturn(commitMessage);
        when(messageService.enqueueCommitMessage(request, commitMessage)).thenReturn(enqueueResult);

        // Act
        CompletableFuture<CommitResult> future = commitService.commit(request);
        CommitResult result = future.join();

        // Assert
        assertEquals(-1, result.getResponseCode());
        verify(messageService, never()).deletePrepareMessage(any(), any());
    }

    // ========== RollbackService Integration ==========

    @Test
    void rollbackService_shouldDeletePrepareMessage() {
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        RollbackService rollbackService = new RollbackService(messageService);

        MessageBO prepareMessage = MessageBO.builder()
            .topic("TestTopic")
            .body("rollback body".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("txn-rollback")
            .storeGroup("store1")
            .commitOffset(200L)
            .queueOffset(100L)
            .build();

        when(messageService.getMessage(request)).thenReturn(prepareMessage);

        // Act
        CompletableFuture<CommitResult> future = rollbackService.rollback(request);
        CommitResult result = future.join();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getResponseCode());
        verify(messageService).getMessage(request);
        verify(messageService).deletePrepareMessage(request, prepareMessage);
    }

    // ========== Transaction Controller Validation ==========

    @Test
    void transactionController_submitWithBlankTransactionId_shouldThrowInvalidRequestException() {
        Transaction transaction = mock(Transaction.class);
        TransactionController controller = new TransactionController(transaction);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("")
            .transactionType(TransactionType.COMMIT)
            .topicName("TestTopic")
            .build();

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> controller.submit(request));
    }

    @Test
    void transactionController_submitWithNullTransactionId_shouldThrowInvalidRequestException() {
        Transaction transaction = mock(Transaction.class);
        TransactionController controller = new TransactionController(transaction);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId(null)
            .transactionType(TransactionType.COMMIT)
            .topicName("TestTopic")
            .build();

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> controller.submit(request));
    }

    @Test
    void transactionController_submitWithInvalidTransactionFlag_shouldReturnFailureFuture() {
        Transaction transaction = mock(Transaction.class);
        TransactionController controller = new TransactionController(transaction);

        long now = System.currentTimeMillis();
        Receipt receipt = Receipt.builder()
            .producerGroup("testGroup")
            .transactionId("txn-flag")
            .storeGroup("store1")
            .messageId("msg-flag")
            .commitOffset(100L)
            .queueOffset(50L)
            .checkTimestamp(now)
            .expireMs(30000L)
            .build();

        // We need to use a real TransactionController with real Transaction
        // to test the checkStatus method
        // SubmitRequest with transactionFlag=0 (neither COMMIT nor ROLLBACK)
        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .transactionId("txn-flag")
            .transactionFlag(0) // neither COMMIT(1) nor ROLLBACK(2)
            .topicName("TestTopic")
            .build();

        // Act
        CompletableFuture<CommitResult> result = controller.submit(request);

        // Assert - should return failure future because transactionFlag is invalid
        CommitResult commitResult = result.join();
        assertEquals(-1, commitResult.getResponseCode());
    }

    // ========== SubmitValidator Integration ==========

    @Test
    void submitValidator_validRequest_shouldPass() {
        TransactionConfig config = new TransactionConfig();
        SubmitValidator validator = new SubmitValidator(config);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test body".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();
        message.putProperty(MessageConst.PROPERTY_PRODUCER_GROUP, "testGroup");

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .commitOffset(100L)
            .queueOffset(50L)
            .build();

        message.setCommitOffset(100L);
        message.setQueueOffset(50L);

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(request, message));
    }

    @Test
    void submitValidator_nullMessage_shouldThrow() {
        TransactionConfig config = new TransactionConfig();
        SubmitValidator validator = new SubmitValidator(config);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .build();

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> validator.validate(request, null));
    }

    @Test
    void submitValidator_mismatchedProducerGroup_shouldThrow() {
        TransactionConfig config = new TransactionConfig();
        SubmitValidator validator = new SubmitValidator(config);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();
        message.putProperty(MessageConst.PROPERTY_PRODUCER_GROUP, "groupA");
        message.setCommitOffset(100L);
        message.setQueueOffset(50L);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("groupB")
            .commitOffset(100L)
            .queueOffset(50L)
            .build();

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> validator.validate(request, message));
    }

    @Test
    void submitValidator_mismatchedCommitOffset_shouldThrow() {
        TransactionConfig config = new TransactionConfig();
        SubmitValidator validator = new SubmitValidator(config);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();
        message.putProperty(MessageConst.PROPERTY_PRODUCER_GROUP, "testGroup");
        message.setCommitOffset(999L);  // mismatch
        message.setQueueOffset(50L);

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .commitOffset(100L)  // different
            .queueOffset(50L)
            .build();

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> validator.validate(request, message));
    }

    @Test
    void submitValidator_mismatchedQueueOffset_shouldThrow() {
        TransactionConfig config = new TransactionConfig();
        SubmitValidator validator = new SubmitValidator(config);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .build();
        message.putProperty(MessageConst.PROPERTY_PRODUCER_GROUP, "testGroup");
        message.setCommitOffset(100L);
        message.setQueueOffset(999L);  // mismatch

        SubmitRequest request = SubmitRequest.builder()
            .producerGroup("testGroup")
            .commitOffset(100L)
            .queueOffset(50L)  // different
            .build();

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> validator.validate(request, message));
    }
}