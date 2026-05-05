package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.broker.api.ProducerController;
import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.broker.domain.producer.EnqueueService;
import cn.coderule.wolfmq.broker.domain.producer.ProduceHookManager;
import cn.coderule.wolfmq.broker.domain.producer.Producer;
import cn.coderule.wolfmq.broker.domain.producer.ProducerManager;
import cn.coderule.wolfmq.broker.domain.producer.QueueSelector;
import cn.coderule.wolfmq.broker.domain.transaction.Transaction;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.enums.code.InvalidCode;
import cn.coderule.wolfmq.domain.core.enums.message.CleanupPolicy;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.selector.MessageQueueSelector;
import cn.coderule.wolfmq.domain.domain.cluster.selector.MessageQueueView;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.producer.ProduceContext;
import cn.coderule.wolfmq.domain.domain.producer.hook.ProduceHook;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import io.netty.channel.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for the Producer flow.
 * Wires up real domain services together, mocking only infrastructure
 * (MQFacade, TopicFacade, Transaction, RouteService).
 */
class ProducerFlowIntegrationTest {

    private BrokerConfig brokerConfig;
    private MessageConfig messageConfig;
    private TimerConfig timerConfig;

    private MQFacade mqFacade;
    private TopicFacade topicFacade;
    private Transaction transaction;
    private RouteService routeService;

    private ProduceHookManager hookManager;
    private QueueSelector queueSelector;
    private EnqueueService enqueueService;
    private ProducerManager producerManager;
    private Producer producer;
    private ProducerController controller;

    @BeforeEach
    void setUp() throws Exception {
        messageConfig = new MessageConfig();
        timerConfig = new TimerConfig();

        brokerConfig = mock(BrokerConfig.class);
        when(brokerConfig.getMessageConfig()).thenReturn(messageConfig);
        when(brokerConfig.getTimerConfig()).thenReturn(timerConfig);
        when(brokerConfig.getProducerThreadNum()).thenReturn(2);
        when(brokerConfig.getProducerQueueCapacity()).thenReturn(100);
        when(brokerConfig.getHost()).thenReturn("localhost");
        when(brokerConfig.getPort()).thenReturn(10911);
        when(brokerConfig.getCluster()).thenReturn("DefaultCluster");
        when(brokerConfig.getServerReadyTime()).thenReturn(0L);
        when(brokerConfig.getChannelExpireTime()).thenReturn(120_000L);
        when(brokerConfig.getMaxChannelFetchTimes()).thenReturn(3);
        when(brokerConfig.isEnableTrace()).thenReturn(false);
        when(brokerConfig.getRegion()).thenReturn("DefaultRegion");

        mqFacade = mock(MQFacade.class);
        topicFacade = mock(TopicFacade.class);
        transaction = mock(Transaction.class);
        routeService = mock(RouteService.class);

        hookManager = new ProduceHookManager();
        queueSelector = new QueueSelector(routeService);
        enqueueService = new EnqueueService(
            brokerConfig, hookManager, queueSelector, mqFacade, topicFacade, transaction
        );
        producerManager = new ProducerManager(brokerConfig);
        producer = new Producer(enqueueService, producerManager);
        controller = new ProducerController(brokerConfig, producer);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (enqueueService != null) {
            enqueueService.shutdown();
        }
    }

    // ========== Helper methods ==========

    private Topic createCompactionTopic(String topicName) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cleanup.policy", "COMPACTION");
        return Topic.builder()
            .topicName(topicName)
            .readQueueNums(4)
            .writeQueueNums(4)
            .attributes(attributes)
            .build();
    }

    private Topic createDeleteTopic(String topicName) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("cleanup.policy", "DELETE");
        return Topic.builder()
            .topicName(topicName)
            .readQueueNums(4)
            .writeQueueNums(4)
            .attributes(attributes)
            .build();
    }

    private MessageBO createValidMessage(String topic, String body, String keys) {
        MessageBO message = MessageBO.builder()
            .topic(topic)
            .body(body.getBytes())
            .build();
        if (keys != null) {
            message.setKeys(keys);
        }
        return message;
    }

    private MessageQueue createDefaultMessageQueue(String topicName) {
        return MessageQueue.builder()
            .topicName(topicName)
            .groupName("DefaultCluster")
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

    private RequestContext createRequestContext() {
        return RequestContext.create("testProducerGroup");
    }

    /**
     * Sets up the full infrastructure mocks for a successful produce flow:
     * - TopicFacade returns a COMPACTION topic
     * - RouteService returns a MessageQueueView with queues
     * - MQFacade returns a successful EnqueueResult
     */
    private void setupSuccessfulProduceMocks(String topicName) {
        Topic topic = createCompactionTopic(topicName);
        when(topicFacade.getTopic(topicName)).thenReturn(topic);

        MessageQueueView queueView = createQueueView(topicName);
        when(routeService.getQueueView(any(RequestContext.class), eq(topicName)))
            .thenReturn(queueView);

        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(createSuccessEnqueueResult()));
    }

    private MessageQueueView createQueueView(String topicName) {
        cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo routeInfo = new cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo();
        routeInfo.setTopicName(topicName);

        cn.coderule.wolfmq.domain.domain.cluster.server.GroupInfo groupInfo =
            new cn.coderule.wolfmq.domain.domain.cluster.server.GroupInfo("DefaultCluster", "DefaultGroup");
        Map<Long, String> brokerAddrs = new HashMap<>();
        brokerAddrs.put(0L, "localhost:10911");
        groupInfo.setBrokerAddrs(brokerAddrs);
        routeInfo.getBrokerDatas().add(groupInfo);

        cn.coderule.wolfmq.domain.domain.cluster.route.QueueInfo queueInfo =
            cn.coderule.wolfmq.domain.domain.cluster.route.QueueInfo.from("DefaultGroup", createCompactionTopic(topicName));
        routeInfo.getQueueDatas().add(queueInfo);

        return new MessageQueueView(topicName, routeInfo, null);
    }

    // ========== Scenario 1: Produce single message success ==========

    @Test
    void produce_singleMessage_success_returnsEnqueueResult() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);

        assertNotNull(future);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(EnqueueStatus.PUT_OK, result.getStatus());
    }

    @Test
    void produce_singleMessage_success_setsRegionOnResult() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);

        assertEquals("DefaultRegion", result.getRegion());
    }

    @Test
    void produce_singleMessage_success_setsUniqueKeyIfAbsent() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");
        // unique key should be null initially
        assertNull(message.getUniqueKey());

        controller.produce(context, message);

        assertNotNull(message.getUniqueKey());
    }

    @Test
    void produce_singleMessage_success_preservesExistingUniqueKey() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");
        message.setUniqueKey("my-unique-key");

        controller.produce(context, message);

        assertEquals("my-unique-key", message.getUniqueKey());
    }

    @Test
    void produce_singleMessage_success_callsPreAndPostHooks() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        ProduceHook trackingHook = mock(ProduceHook.class);
        when(trackingHook.hookName()).thenReturn("TrackingHook");
        hookManager.registerHook(trackingHook);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);
        future.get(5, TimeUnit.SECONDS);

        // Allow async callback to execute
        Thread.sleep(300);

        verify(trackingHook).preProduce(any(ProduceContext.class));
        verify(trackingHook).postProduce(any(ProduceContext.class));
    }

    @Test
    void produce_singleMessage_success_storesMessageViaMQFacade() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        controller.produce(context, message);

        verify(mqFacade).enqueueAsync(any(EnqueueRequest.class));
    }

    // ========== Scenario 2: Produce with invalid topic ==========

    @Test
    void produce_blankTopic_throwsInvalidParameterException() {
        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage("", "hello", "key1");

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> controller.produce(context, message)
        );

        assertEquals(InvalidCode.ILLEGAL_TOPIC, exception.getInvalidCode());
    }

    @Test
    void produce_nullTopic_throwsInvalidParameterException() {
        RequestContext context = createRequestContext();
        MessageBO message = MessageBO.builder()
            .body("hello".getBytes())
            .build();

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> controller.produce(context, message)
        );

        assertEquals(InvalidCode.ILLEGAL_TOPIC, exception.getInvalidCode());
    }

    @Test
    void produce_topicWithIllegalChars_throwsInvalidParameterException() {
        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage("Invalid@Topic!", "hello", "key1");

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> controller.produce(context, message)
        );

        assertEquals(InvalidCode.ILLEGAL_TOPIC, exception.getInvalidCode());
    }

    // ========== Scenario 3: Produce batch messages ==========

    @Test
    void produce_batchMessages_success_returnsCombinedResults() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO msg1 = createValidMessage(topicName, "body1", "key1");
        MessageBO msg2 = createValidMessage(topicName, "body2", "key2");
        List<MessageBO> messages = List.of(msg1, msg2);

        CompletableFuture<List<EnqueueResult>> future = controller.produce(context, messages);

        assertNotNull(future);
        List<EnqueueResult> results = future.get(5, TimeUnit.SECONDS);
        assertNotNull(results);
        assertEquals(2, results.size());
        for (EnqueueResult result : results) {
            assertTrue(result.isSuccess());
        }
    }

    @Test
    void produce_emptyBatch_throwsInvalidParameterException() {
        RequestContext context = createRequestContext();
        List<MessageBO> emptyList = new ArrayList<>();

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> controller.produce(context, emptyList)
        );

        assertEquals(InvalidCode.BAD_REQUEST, exception.getInvalidCode());
    }

    // ========== Scenario 4: Produce with message body too large ==========

    @Test
    void produce_bodyTooLarge_throwsInvalidRequestException() {
        messageConfig.setMaxBodySize(10);

        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        // Create a message with body larger than maxBodySize
        byte[] largeBody = new byte[100];
        MessageBO message = createValidMessage(topicName, new String(largeBody), "key1");

        InvalidRequestException exception = assertThrows(
            InvalidRequestException.class,
            () -> controller.produce(context, message)
        );

        assertEquals(InvalidCode.MESSAGE_BODY_TOO_LARGE, exception.getInvalidCode());
    }

    // ========== Scenario 5: Produce with reserved properties ==========

    @Test
    void produce_withPopCkProperty_cleansReservedProperty() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");
        message.putProperty(MessageConst.PROPERTY_POP_CK, "some-pop-check-value");

        assertNotNull(message.getProperty(MessageConst.PROPERTY_POP_CK));

        controller.produce(context, message);

        assertNull(message.getProperty(MessageConst.PROPERTY_POP_CK));
    }

    // ========== Scenario 6: Producer register/unregister lifecycle ==========

    @Test
    void register_andUnregister_producer_success() {
        Channel mockChannel = mock(Channel.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);

        ClientChannelInfo channelInfo = ClientChannelInfo.builder()
            .clientId("client-1")
            .channel(mockChannel)
            .build();

        RequestContext context = createRequestContext();
        String groupName = "testProducerGroup";

        // Register
        assertDoesNotThrow(() -> controller.register(context, groupName, channelInfo));
        assertTrue(producerManager.isGroupExist(groupName));

        // Unregister
        assertDoesNotThrow(() -> controller.unregister(context, groupName, channelInfo));
        assertFalse(producerManager.isGroupExist(groupName));
    }

    @Test
    void register_multipleProducers_sameGroup_success() {
        Channel mockChannel1 = mock(Channel.class);
        Channel mockChannel2 = mock(Channel.class);
        when(mockChannel1.isActive()).thenReturn(true);
        when(mockChannel1.isWritable()).thenReturn(true);
        when(mockChannel2.isActive()).thenReturn(true);
        when(mockChannel2.isWritable()).thenReturn(true);

        ClientChannelInfo channelInfo1 = ClientChannelInfo.builder()
            .clientId("client-1")
            .channel(mockChannel1)
            .build();
        ClientChannelInfo channelInfo2 = ClientChannelInfo.builder()
            .clientId("client-2")
            .channel(mockChannel2)
            .build();

        RequestContext context = createRequestContext();
        String groupName = "testProducerGroup";

        controller.register(context, groupName, channelInfo1);
        controller.register(context, groupName, channelInfo2);

        assertTrue(producerManager.isGroupExist(groupName));
        assertEquals(1, producerManager.getGroupCount());
    }

    @Test
    void register_withBlankTopic_throwsInvalidParameterException() {
        Channel mockChannel = mock(Channel.class);
        ClientChannelInfo channelInfo = ClientChannelInfo.builder()
            .clientId("client-1")
            .channel(mockChannel)
            .build();

        RequestContext context = createRequestContext();

        assertThrows(
            InvalidParameterException.class,
            () -> controller.register(context, "", channelInfo)
        );
    }

    // ========== Scenario 7: EnqueueService integration with QueueSelector ==========

    @Test
    void enqueueService_withRealQueueSelector_selectsQueueAndStores() throws Exception {
        String topicName = "TestTopic";
        Topic topic = createCompactionTopic(topicName);
        when(topicFacade.getTopic(topicName)).thenReturn(topic);

        MessageQueueView queueView = createQueueView(topicName);
        when(routeService.getQueueView(any(RequestContext.class), eq(topicName)))
            .thenReturn(queueView);

        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(createSuccessEnqueueResult()));

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(context, message);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
        verify(routeService).getQueueView(any(RequestContext.class), eq(topicName));
        verify(mqFacade).enqueueAsync(any(EnqueueRequest.class));
    }

    @Test
    void enqueueService_topicNotFound_throwsInvalidParameterException() {
        String topicName = "NonExistentTopic";
        when(topicFacade.getTopic(topicName)).thenReturn(null);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", "key1");

        assertThrows(
            InvalidParameterException.class,
            () -> enqueueService.enqueue(context, message)
        );
    }

    @Test
    void enqueueService_deletePolicyWithoutKeys_throwsInvalidParameterException() {
        String topicName = "DeleteTopic";
        Topic deleteTopic = createDeleteTopic(topicName);
        when(topicFacade.getTopic(topicName)).thenReturn(deleteTopic);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", null);

        assertThrows(
            InvalidParameterException.class,
            () -> enqueueService.enqueue(context, message)
        );
    }

    @Test
    void enqueueService_deletePolicyWithKeys_succeeds() throws Exception {
        String topicName = "DeleteTopic";
        Topic deleteTopic = createDeleteTopic(topicName);
        when(topicFacade.getTopic(topicName)).thenReturn(deleteTopic);

        MessageQueueView queueView = createQueueView(topicName);
        when(routeService.getQueueView(any(RequestContext.class), eq(topicName)))
            .thenReturn(queueView);

        when(mqFacade.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(createSuccessEnqueueResult()));

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", "key1");

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(context, message);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
    }

    @Test
    void enqueueService_prepareMessage_callsTransactionPrepare() throws Exception {
        String topicName = "TestTopic";
        Topic topic = createCompactionTopic(topicName);
        when(topicFacade.getTopic(topicName)).thenReturn(topic);

        MessageQueueView queueView = createQueueView(topicName);
        when(routeService.getQueueView(any(RequestContext.class), eq(topicName)))
            .thenReturn(queueView);

        EnqueueResult prepareResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId("prepare-msg-id")
            .queueId(0)
            .queueOffset(0L)
            .build();
        when(transaction.prepare(any(RequestContext.class), any(MessageBO.class)))
            .thenReturn(CompletableFuture.completedFuture(prepareResult));

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", "key1");
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");

        CompletableFuture<EnqueueResult> future = enqueueService.enqueue(context, message);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
        verify(transaction).prepare(any(RequestContext.class), any(MessageBO.class));
        verify(mqFacade, never()).enqueueAsync(any(EnqueueRequest.class));
    }

    // ========== Scenario 8: Server not ready ==========

    @Test
    void produce_serverNotReady_throwsInvalidParameterException() {
        when(brokerConfig.getServerReadyTime()).thenReturn(System.currentTimeMillis() + 60000L);

        String topicName = "TestTopic";
        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", "key1");

        InvalidParameterException exception = assertThrows(
            InvalidParameterException.class,
            () -> controller.produce(context, message)
        );

        assertEquals(InvalidCode.INTERNAL_ERROR, exception.getInvalidCode());
    }

    @Test
    void produce_serverReadyTimeZero_succeeds() throws Exception {
        when(brokerConfig.getServerReadyTime()).thenReturn(0L);

        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
    }

    @Test
    void produce_serverReadyTimeInPast_succeeds() throws Exception {
        when(brokerConfig.getServerReadyTime()).thenReturn(System.currentTimeMillis() - 60000L);

        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);
        EnqueueResult result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
    }

    // ========== Additional integration scenarios ==========

    @Test
    void produce_fullFlow_setsStoreHostAndClusterOnMessage() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        controller.produce(context, message);

        assertNotNull(message.getStoreHost());
        assertEquals("DefaultCluster", message.getClusterName());
    }

    @Test
    void produce_fullFlow_setsQueueIdOnMessage() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        controller.produce(context, message);

        assertEquals(0, message.getQueueId());
    }

    @Test
    void produce_batchWithInvalidTopicInList_throwsInvalidParameterException() {
        RequestContext context = createRequestContext();
        MessageBO validMsg = createValidMessage("ValidTopic", "body", "key1");
        MessageBO invalidMsg = MessageBO.builder()
            .body("body".getBytes())
            .build();

        List<MessageBO> messages = List.of(validMsg, invalidMsg);

        assertThrows(
            InvalidParameterException.class,
            () -> controller.produce(context, messages)
        );
    }

    @Test
    void produce_hookManagerReceivesPreAndPostProduceCalls() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        List<String> hookCalls = new ArrayList<>();
        ProduceHook trackingHook = new ProduceHook() {
            @Override
            public String hookName() {
                return "TrackingHook";
            }

            @Override
            public void preProduce(ProduceContext context) {
                hookCalls.add("preProduce");
            }

            @Override
            public void postProduce(ProduceContext context) {
                hookCalls.add("postProduce");
            }
        };
        hookManager.registerHook(trackingHook);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);
        future.get(5, TimeUnit.SECONDS);

        // Allow async callback to execute
        Thread.sleep(300);

        assertTrue(hookCalls.contains("preProduce"), "preProduce hook should be called");
        assertTrue(hookCalls.contains("postProduce"), "postProduce hook should be called");
    }

    @Test
    void produce_multipleHooks_allCalled() throws Exception {
        String topicName = "TestTopic";
        setupSuccessfulProduceMocks(topicName);

        ProduceHook hook1 = mock(ProduceHook.class);
        when(hook1.hookName()).thenReturn("Hook1");
        ProduceHook hook2 = mock(ProduceHook.class);
        when(hook2.hookName()).thenReturn("Hook2");

        hookManager.registerHook(hook1);
        hookManager.registerHook(hook2);

        RequestContext context = createRequestContext();
        MessageBO message = createValidMessage(topicName, "hello world", "key1");

        CompletableFuture<EnqueueResult> future = controller.produce(context, message);
        future.get(5, TimeUnit.SECONDS);

        Thread.sleep(300);

        verify(hook1).preProduce(any(ProduceContext.class));
        verify(hook2).preProduce(any(ProduceContext.class));
        verify(hook1).postProduce(any(ProduceContext.class));
        verify(hook2).postProduce(any(ProduceContext.class));
    }
}
