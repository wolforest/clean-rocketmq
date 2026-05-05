package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.broker.domain.transaction.Transaction;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.enums.message.CleanupPolicy;
import cn.coderule.wolfmq.domain.core.enums.message.MessageType;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.core.enums.code.InvalidCode;
import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;

import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnqueueServiceTest {

    private BrokerConfig brokerConfig;
    private ProduceHookManager hookManager;
    private QueueSelector queueSelector;
    private MQFacade mqFacade;
    private TopicFacade topicFacade;
    private Transaction transaction;
    private EnqueueService enqueueService;

    @BeforeEach
    void setUp() {
        brokerConfig = mock(BrokerConfig.class);
        hookManager = mock(ProduceHookManager.class);
        queueSelector = mock(QueueSelector.class);
        mqFacade = mock(MQFacade.class);
        topicFacade = mock(TopicFacade.class);
        transaction = mock(Transaction.class);

        when(brokerConfig.getProducerThreadNum()).thenReturn(4);
        when(brokerConfig.getProducerQueueCapacity()).thenReturn(100);
        when(brokerConfig.getHost()).thenReturn("localhost");
        when(brokerConfig.getPort()).thenReturn(10911);
        when(brokerConfig.getCluster()).thenReturn("default");

        enqueueService = new EnqueueService(
            brokerConfig,
            hookManager,
            queueSelector,
            mqFacade,
            topicFacade,
            transaction
        );
    }

    private Topic createDefaultTopic() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cleanup.policy", "COMPACTION");
        return Topic.builder()
            .topicName("TestTopic")
            .readQueueNums(16)
            .writeQueueNums(16)
            .attributes(attributes)
            .build();
    }

    private Topic createCompactionTopic() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cleanup.policy", "COMPACTION");
        return Topic.builder()
            .topicName("CompactionTopic")
            .readQueueNums(16)
            .writeQueueNums(16)
            .attributes(attributes)
            .build();
    }

    private Topic createDeleteTopic() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cleanup.policy", "DELETE");
        return Topic.builder()
            .topicName("DeleteTopic")
            .readQueueNums(16)
            .writeQueueNums(16)
            .attributes(attributes)
            .build();
    }

    private MessageBO createDefaultMessage() {
        return MessageBO.builder()
            .topic("TestTopic")
            .body("test body".getBytes())
            .build();
    }

    private MessageBO createMessageWithKeys(String keys) {
        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test body".getBytes())
            .build();
        message.setKeys(keys);
        return message;
    }

    private MessageBO createPrepareMessage() {
        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test body".getBytes())
            .build();
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        return message;
    }

    private MessageQueue createDefaultMessageQueue() {
        return MessageQueue.builder()
            .topicName("TestTopic")
            .groupName("default")
            .queueId(0)
            .build();
    }

    private EnqueueResult createSuccessEnqueueResult() {
        return EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId("test-msg-id")
            .queueId(0)
            .queueOffset(0L)
            .build();
    }

    private RequestContext createDefaultRequestContext() {
        return RequestContext.create("testGroup");
    }

    private void setupDefaultTopicAndQueue(MessageBO message) {
        when(topicFacade.getTopic(message.getTopic())).thenReturn(createDefaultTopic());
        when(queueSelector.select(any(RequestContext.class), any(MessageBO.class)))
            .thenReturn(createDefaultMessageQueue());
    }

    // ========== Single message enqueue tests ==========

    @Test
    void enqueue_singleMessage_success_shouldCallMQFacadeAndHooks() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(requestContext, message);

        assertNotNull(future);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);
        assertTrue(result.isSuccess());

        // Verify MQFacade.enqueueAsync was called
        verify(mqFacade).enqueueAsync(any(EnqueueRequest.class));

        // Verify hooks were triggered
        verify(hookManager).preProduce(any());
    }

    @Test
    void enqueue_singleMessage_success_shouldSetStoreHostAndCluster() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        enqueueService.enqueue(requestContext, message);

        // Verify storeHost and clusterName were set on the message
        assertNotNull(message.getStoreHost());
        assertEquals("default", message.getClusterName());
    }

    @Test
    void enqueue_singleMessage_success_shouldSetQueueId() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        enqueueService.enqueue(requestContext, message);

        // Queue ID is set to 0 in the source code (line 186)
        assertEquals(0, message.getQueueId());
    }

    // ========== Topic not found tests ==========

    @Test
    void enqueue_topicNotFound_shouldThrowInvalidParameterException() {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();

        when(topicFacade.getTopic(message.getTopic())).thenReturn(null);

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> enqueueService.enqueue(requestContext, message)
        );

        assertEquals(InvalidCode.ILLEGAL_TOPIC, exception.getInvalidCode());
    }

    // ========== Cleanup policy tests ==========

    @Test
    void enqueue_deletePolicyWithNoKeys_shouldThrowInvalidParameterException() {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage(); // no keys set

        Topic deleteTopic = createDeleteTopic();
        when(topicFacade.getTopic(message.getTopic())).thenReturn(deleteTopic);

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> enqueueService.enqueue(requestContext, message)
        );

        assertEquals(InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY, exception.getInvalidCode());
    }

    @Test
    void enqueue_deletePolicyWithKeys_shouldSucceed() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createMessageWithKeys("test-key");

        Topic deleteTopic = createDeleteTopic();
        when(topicFacade.getTopic(message.getTopic())).thenReturn(deleteTopic);
        when(queueSelector.select(any(RequestContext.class), any(MessageBO.class)))
            .thenReturn(createDefaultMessageQueue());

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(requestContext, message);

        assertNotNull(future);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);
        assertTrue(result.isSuccess());
    }

    @Test
    void enqueue_compactionPolicyWithNoKeys_shouldNotThrow() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage(); // no keys set

        Topic compactionTopic = createCompactionTopic();
        when(topicFacade.getTopic(message.getTopic())).thenReturn(compactionTopic);
        when(queueSelector.select(any(RequestContext.class), any(MessageBO.class)))
            .thenReturn(createDefaultMessageQueue());

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(requestContext, message);

        assertNotNull(future);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);
        assertTrue(result.isSuccess());
    }

    // ========== Prepare message (transaction) tests ==========

    @Test
    void enqueue_prepareMessage_shouldCallTransactionPrepare() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createPrepareMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult prepareResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId("prepare-msg-id")
            .queueId(0)
            .queueOffset(0L)
            .build();

        when(transaction.prepare(any(RequestContext.class), any(MessageBO.class)))
            .thenReturn(CompletableFuture.completedFuture(prepareResult));

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(requestContext, message);

        assertNotNull(future);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);
        assertTrue(result.isSuccess());

        // Verify transaction.prepare was called instead of mqFacade.enqueueAsync
        verify(transaction).prepare(any(RequestContext.class), any(MessageBO.class));
        verify(mqFacade, never()).enqueueAsync(any(EnqueueRequest.class));
    }

    // ========== Batch enqueue tests ==========

    @Test
    void enqueueBatch_emptyList_shouldReturnEmptyResultImmediately() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        List<MessageBO> emptyList = new ArrayList<>();

        CompletableFuture<List<EnqueueResult>> future = enqueueService.enqueue(requestContext, emptyList);

        assertNotNull(future);
        List<EnqueueResult> results = future.get(5, TimeUnit.SECONDS);
        assertTrue(results.isEmpty());

        // No interactions with MQFacade or topicFacade for empty list
        verify(mqFacade, never()).enqueueAsync(any(EnqueueRequest.class));
        verify(topicFacade, never()).getTopic(anyString());
    }

    @Test
    void enqueueBatch_multipleMessages_shouldCombineResults() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();

        MessageBO message1 = MessageBO.builder()
            .topic("TestTopic")
            .body("body1".getBytes())
            .build();
        message1.setKeys("key1");

        MessageBO message2 = MessageBO.builder()
            .topic("TestTopic")
            .body("body2".getBytes())
            .build();
        message2.setKeys("key2");

        List<MessageBO> messages = List.of(message1, message2);

        Topic topic = createDefaultTopic();
        when(topicFacade.getTopic("TestTopic")).thenReturn(topic);
        when(queueSelector.select(any(RequestContext.class), any(MessageBO.class)))
            .thenReturn(createDefaultMessageQueue());

        EnqueueResult result1 = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId("msg-id-1")
            .queueId(0)
            .queueOffset(0L)
            .build();
        EnqueueResult result2 = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId("msg-id-2")
            .queueId(0)
            .queueOffset(1L)
            .build();

        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(result1))
            .thenReturn(CompletableFuture.completedFuture(result2));

        CompletableFuture<List<EnqueueResult>> future = enqueueService.enqueue(requestContext, messages);

        assertNotNull(future);
        List<EnqueueResult> results = future.get(5, TimeUnit.SECONDS);
        assertEquals(2, results.size());
    }

    // ========== Shutdown tests ==========

    @Test
    void shutdown_shouldNotThrow() {
        assertDoesNotThrow(() -> enqueueService.shutdown());
    }

    // ========== Queue selection tests ==========

    @Test
    void enqueue_shouldSelectQueueForMessage() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        enqueueService.enqueue(requestContext, message);

        verify(queueSelector).select(any(RequestContext.class), eq(message));
    }

    // ========== Pre/Post hook tests ==========

    @Test
    void enqueue_shouldCallPreProduceHook() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        enqueueService.enqueue(requestContext, message);

        verify(hookManager).preProduce(any());
    }

    @Test
    void enqueue_shouldCallPostProduceHookOnSuccess() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(requestContext, message);
        future.get(5, TimeUnit.SECONDS);

        // Give the async callback time to execute
        Thread.sleep(200);

        verify(hookManager).postProduce(any());
    }

    // ========== Default cleanup policy (no attributes) tests ==========

    @Test
    void enqueue_defaultCleanupPolicyWithNoKeys_shouldThrowInvalidParameterException() {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage(); // no keys

        // Topic with no attributes defaults to DELETE cleanup policy
        Topic noAttributesTopic = Topic.builder()
            .topicName("TestTopic")
            .readQueueNums(16)
            .writeQueueNums(16)
            .build();
        when(topicFacade.getTopic(message.getTopic())).thenReturn(noAttributesTopic);

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> enqueueService.enqueue(requestContext, message)
        );

        assertEquals(InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY, exception.getInvalidCode());
    }

    // ========== EnqueueRequest creation test ==========

    @Test
    void enqueue_normalMessage_shouldCreateEnqueueRequestWithMessage() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult enqueueResult = createSuccessEnqueueResult();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        enqueueService.enqueue(requestContext, message);

        ArgumentCaptor<EnqueueRequest> requestCaptor = ArgumentCaptor.forClass(EnqueueRequest.class);
        verify(mqFacade).enqueueAsync(requestCaptor.capture());

        EnqueueRequest capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest);
        assertEquals(message, capturedRequest.getMessageBO());
    }

    // ========== Failure result test ==========

    @Test
    void enqueue_mqFacadeReturnsFailure_shouldReturnFailureResult() throws Exception {
        RequestContext requestContext = createDefaultRequestContext();
        MessageBO message = createDefaultMessage();
        setupDefaultTopicAndQueue(message);

        EnqueueResult failureResult = EnqueueResult.failure();
        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(failureResult));

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(requestContext, message);

        assertNotNull(future);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);
        assertFalse(result.isSuccess());
    }
}