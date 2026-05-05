package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.transaction.CommitBuffer;
import cn.coderule.wolfmq.domain.domain.transaction.OffsetQueue;
import cn.coderule.wolfmq.domain.domain.transaction.SubmitRequest;
import cn.coderule.wolfmq.domain.domain.transaction.TransactionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageFactoryTest {

    private BrokerConfig brokerConfig;
    private TransactionConfig transactionConfig;
    private CommitBuffer commitBuffer;
    private MessageFactory messageFactory;
    private SocketAddress hostAddress;

    @BeforeEach
    void setUp() {
        brokerConfig = mock(BrokerConfig.class);
        transactionConfig = mock(TransactionConfig.class);
        commitBuffer = mock(CommitBuffer.class);
        hostAddress = new InetSocketAddress("127.0.0.1", 10911);

        when(brokerConfig.getTransactionConfig()).thenReturn(transactionConfig);
        when(brokerConfig.getHostAddress()).thenReturn(hostAddress);
        when(transactionConfig.getMaxCommitMessageLength()).thenReturn(4096);

        messageFactory = new MessageFactory(brokerConfig, commitBuffer);
    }

    // ========== createPrepareMessage ==========

    @Test
    void createPrepareMessage_withUniqueKey_setsTransactionIdAndProperties() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(3)
            .body("test body".getBytes(StandardCharsets.UTF_8))
            .build();
        msg.setUniqueKey("unique-key-123");

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertEquals("unique-key-123", result.getTransactionId());
        assertEquals("TestTopic", result.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        assertEquals("3", result.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID));
        assertEquals(TransactionUtil.buildPrepareTopic(), result.getTopic());
        assertEquals(0, result.getQueueId());
        assertEquals(MessageSysFlag.NORMAL_MESSAGE,
            MessageSysFlag.getTransactionType(result.getSysFlag()));
        assertNotNull(result.getPropertiesString());
    }

    @Test
    void createPrepareMessage_withUniqueKey_preservesUniqueKey() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(1)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        msg.setUniqueKey("my-unique-key");

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertEquals("my-unique-key", result.getUniqueKey());
        assertEquals("my-unique-key", result.getTransactionId());
    }

    @Test
    void createPrepareMessage_withoutUniqueKey_generatesOne() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(5)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertNotNull(result.getUniqueKey());
        assertFalse(result.getUniqueKey().isEmpty());
        assertEquals(result.getUniqueKey(), result.getTransactionId());
    }

    @Test
    void createPrepareMessage_withEmptyUniqueKey_generatesOne() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(2)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        msg.setUniqueKey("");

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertNotNull(result.getUniqueKey());
        assertFalse(result.getUniqueKey().isEmpty());
        assertEquals(result.getUniqueKey(), result.getTransactionId());
    }

    @Test
    void createPrepareMessage_setsSysFlagToNormalMessage() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        msg.setSysFlag(MessageSysFlag.COMMIT_MESSAGE);

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertEquals(MessageSysFlag.NORMAL_MESSAGE,
            MessageSysFlag.getTransactionType(result.getSysFlag()));
    }

    @Test
    void createPrepareMessage_preservesOtherSysFlagBits() {
        int compressedFlag = MessageSysFlag.COMPRESSION_TYPE_COMPARATOR;
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        msg.setSysFlag(compressedFlag);

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertEquals(MessageSysFlag.NORMAL_MESSAGE,
            MessageSysFlag.getTransactionType(result.getSysFlag()));
        assertTrue((result.getSysFlag() & compressedFlag) != 0);
    }

    @Test
    void createPrepareMessage_setsQueueIdToZero() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(7)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertEquals(0, result.getQueueId());
    }

    @Test
    void createPrepareMessage_realTopicAndQueueIdStoredInProperties() {
        MessageBO msg = MessageBO.builder()
            .topic("OriginalTopic")
            .queueId(4)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertEquals("OriginalTopic", result.getRealTopic());
        assertEquals(4, result.getRealQueueId());
    }

    @Test
    void createPrepareMessage_callsInitPropertiesString() {
        MessageBO msg = MessageBO.builder()
            .topic("TestTopic")
            .queueId(1)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        msg.setUniqueKey("key-1");

        MessageBO result = messageFactory.createPrepareMessage(msg);

        assertNotNull(result.getPropertiesString());
        assertTrue(result.getPropertiesString().contains("key-1"));
    }

    // ========== createCommitMessage ==========

    @Test
    void createCommitMessage_copiesBodyAndTopicFromPrepareMessage() {
        byte[] body = "commit body".getBytes(StandardCharsets.UTF_8);
        MessageBO prepareMessage = MessageBO.builder()
            .topic(TransactionUtil.buildPrepareTopic())
            .queueId(0)
            .body(body)
            .bornTimestamp(1000L)
            .bornHost(hostAddress)
            .storeHost(hostAddress)
            .storeTimestamp(2000L)
            .build();
        prepareMessage.setUniqueKey("tx-key-1");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "3");
        prepareMessage.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");

        SubmitRequest request = SubmitRequest.builder()
            .queueOffset(50L)
            .commitOffset(100L)
            .build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertArrayEquals(body, result.getBody());
        assertEquals("RealTopic", result.getTopic());
        assertEquals(3, result.getQueueId());
    }

    @Test
    void createCommitMessage_setsTransactionIdFromPrepareUniqueKey() {
        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        prepareMessage.setUniqueKey("unique-tx-id");
        prepareMessage.setTransactionId("unique-tx-id");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");

        SubmitRequest request = SubmitRequest.builder()
            .queueOffset(10L)
            .commitOffset(20L)
            .build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertEquals("unique-tx-id", result.getTransactionId());
    }

    @Test
    void createCommitMessage_setsQueueOffsetAndPrepareOffset() {
        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        prepareMessage.setUniqueKey("key");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");

        SubmitRequest request = SubmitRequest.builder()
            .queueOffset(55L)
            .commitOffset(110L)
            .build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertEquals(55L, result.getQueueOffset());
        assertEquals(110L, result.getPrepareOffset());
    }

    @Test
    void createCommitMessage_removesTransactionProperties() {
        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        prepareMessage.setUniqueKey("key");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "2");
        prepareMessage.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");

        SubmitRequest request = SubmitRequest.builder().build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertNull(result.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        assertNull(result.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID));
        assertNull(result.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED));
    }

    @Test
    void createCommitMessage_setsSysFlagToCommitMessage() {
        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        prepareMessage.setUniqueKey("key");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");
        prepareMessage.setSysFlag(MessageSysFlag.PREPARE_MESSAGE);

        SubmitRequest request = SubmitRequest.builder().build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertEquals(MessageSysFlag.COMMIT_MESSAGE,
            MessageSysFlag.getTransactionType(result.getSysFlag()));
    }

    @Test
    void createCommitMessage_copiesBornAndStoreInfo() {
        SocketAddress bornHost = new InetSocketAddress("10.0.0.1", 8080);
        SocketAddress storeHost = new InetSocketAddress("10.0.0.2", 9090);

        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .bornTimestamp(12345L)
            .bornHost(bornHost)
            .storeHost(storeHost)
            .storeTimestamp(67890L)
            .build();
        prepareMessage.setUniqueKey("key");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");

        SubmitRequest request = SubmitRequest.builder().build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertEquals(12345L, result.getBornTimestamp());
        assertEquals(bornHost, result.getBornHost());
        assertEquals(storeHost, result.getStoreHost());
        assertEquals(67890L, result.getStoreTimestamp());
    }

    @Test
    void createCommitMessage_setsWaitStoreToFalse() {
        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .build();
        prepareMessage.setUniqueKey("key");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");
        prepareMessage.setWaitStore(false);

        SubmitRequest request = SubmitRequest.builder().build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertFalse(result.isWaitStore());
    }

    @Test
    void createCommitMessage_copiesFlag() {
        MessageBO prepareMessage = MessageBO.builder()
            .topic("topic")
            .queueId(0)
            .body("body".getBytes(StandardCharsets.UTF_8))
            .flag(42)
            .build();
        prepareMessage.setUniqueKey("key");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "RealTopic");
        prepareMessage.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, "0");

        SubmitRequest request = SubmitRequest.builder().build();

        MessageBO result = messageFactory.createCommitMessage(request, prepareMessage);

        assertEquals(42, result.getFlag());
    }

    // ========== createOperationMessage(OffsetQueue, MessageQueue) ==========

    @Test
    void createOperationMessage_withoutQueueOffset_returnsMessageWithOffsetData() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("offset1,", 1000);
        offsetQueue.addAndGet(8);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        assertEquals(TransactionUtil.buildOperationTopic(), result.getTopic());
        assertEquals(0, result.getQueueId());
        assertEquals(TransactionUtil.REMOVE_TAG, result.getTags());
        assertFalse(result.isWaitStore());
    }

    @Test
    void createOperationMessage_withoutQueueOffset_bodyContainsOffsetData() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(1)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        assertTrue(bodyStr.contains("data1,"));
    }

    // ========== createOperationMessage(OffsetQueue, MessageQueue, long queueOffset) ==========

    @Test
    void createOperationMessage_withPositiveQueueOffset_includesOffsetKey() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(2)
            .build();

        long queueOffset = 100L;
        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue, queueOffset);

        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        String expectedOffsetKey = TransactionUtil.buildOffsetKey(queueOffset);
        assertTrue(bodyStr.startsWith(expectedOffsetKey));
    }

    @Test
    void createOperationMessage_withNegativeQueueOffset_noOffsetKey() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue, -1);

        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        assertFalse(bodyStr.matches("^\\d+,"));
        assertTrue(bodyStr.contains("data1,"));
    }

    @Test
    void createOperationMessage_withZeroQueueOffset_includesOffsetKey() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue, 0);

        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        String expectedOffsetKey = TransactionUtil.buildOffsetKey(0);
        assertTrue(bodyStr.startsWith(expectedOffsetKey));
    }

    @Test
    void createOperationMessage_emptyOffsetQueue_returnsNull() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNull(result);
    }

    @Test
    void createOperationMessage_emptyOffsetQueue_withQueueOffset_returnsNull() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue, 50L);
        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        String expectedOffsetKey = TransactionUtil.buildOffsetKey(50L);
        assertTrue(bodyStr.startsWith(expectedOffsetKey));
    }

    @Test
    void createOperationMessage_setsHostAddresses() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        assertEquals(hostAddress, result.getBornHost());
        assertEquals(hostAddress, result.getStoreHost());
    }

    @Test
    void createOperationMessage_setsSysFlagToZero() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        assertEquals(0, result.getSysFlag());
    }

    @Test
    void createOperationMessage_multipleDataItems_allIncludedInBody() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("offsetA,", 1000);
        offsetQueue.addAndGet(8);
        offsetQueue.offer("offsetB,", 1000);
        offsetQueue.addAndGet(8);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        assertTrue(bodyStr.contains("offsetA,"));
        assertTrue(bodyStr.contains("offsetB,"));
    }

    @Test
    void createOperationMessage_withQueueOffset_includesOffsetAndData() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(3)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue, 200L);

        assertNotNull(result);
        assertEquals(3, result.getQueueId());
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        assertTrue(bodyStr.startsWith(TransactionUtil.buildOffsetKey(200L)));
        assertTrue(bodyStr.contains("data1,"));
    }

    @Test
    void createOperationMessage_respectsMaxCommitMessageLength() {
        when(transactionConfig.getMaxCommitMessageLength()).thenReturn(10);

        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("shortData,", 1000);
        offsetQueue.addAndGet(10);
        offsetQueue.offer("overflowData,", 1000);
        offsetQueue.addAndGet(13);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        String bodyStr = new String(result.getBody(), StandardCharsets.UTF_8);
        assertTrue(bodyStr.length() <= 10);
    }

    @Test
    void createOperationMessage_setsUniqueKey() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        MessageBO result = messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertNotNull(result);
        assertNotNull(result.getUniqueKey());
        assertFalse(result.getUniqueKey().isEmpty());
    }

    @Test
    void createOperationMessage_updatesOffsetQueueSize() {
        OffsetQueue offsetQueue = new OffsetQueue(System.currentTimeMillis(), 100);
        offsetQueue.offer("data1,", 1000);
        int initialSize = offsetQueue.getTotalSize();
        offsetQueue.addAndGet(6);

        MessageQueue operationQueue = MessageQueue.builder()
            .topicName(TransactionUtil.buildOperationTopic())
            .groupName("testGroup")
            .queueId(0)
            .build();

        int sizeBefore = offsetQueue.getTotalSize();
        messageFactory.createOperationMessage(offsetQueue, operationQueue);

        assertTrue(offsetQueue.isEmpty());
    }
}