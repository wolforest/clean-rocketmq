package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedMQStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteMQStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQStoreTest {

    private static final String TEST_TOPIC = "test_topic";

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private EmbedMQStore embedMQStore;

    @Mock
    private RemoteMQStore remoteMQStore;

    private MQStore store;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        store = new MQStore(brokerConfig, embedMQStore, remoteMQStore);
    }

    // ==================== enqueue ====================

    @Test
    void testEnqueue_TopicInEmbedStore_DelegatesToEmbed() {
        MessageBO messageBO = MessageBO.builder().topic(TEST_TOPIC).build();
        EnqueueRequest request = EnqueueRequest.builder().messageBO(messageBO).build();
        EnqueueResult expected = EnqueueResult.notAvailable();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.enqueue(request)).thenReturn(expected);

        EnqueueResult result = store.enqueue(request);

        assertEquals(expected, result);
        verify(embedMQStore).enqueue(request);
        verify(remoteMQStore, never()).enqueue(any());
    }

    @Test
    void testEnqueue_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        MessageBO messageBO = MessageBO.builder().topic(TEST_TOPIC).build();
        EnqueueRequest request = EnqueueRequest.builder().messageBO(messageBO).build();
        EnqueueResult expected = EnqueueResult.notAvailable();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.enqueue(request)).thenReturn(expected);

        EnqueueResult result = store.enqueue(request);

        assertEquals(expected, result);
        verify(remoteMQStore).enqueue(request);
    }

    @Test
    void testEnqueue_TopicNotInEmbed_RemoteDisabled_ReturnsNotAvailable() {
        MessageBO messageBO = MessageBO.builder().topic(TEST_TOPIC).build();
        EnqueueRequest request = EnqueueRequest.builder().messageBO(messageBO).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        EnqueueResult result = store.enqueue(request);

        assertFalse(result.isSuccess());
        verify(remoteMQStore, never()).enqueue(any());
        verify(embedMQStore, never()).enqueue(any());
    }

    // ==================== enqueueAsync ====================

    @Test
    void testEnqueueAsync_TopicInEmbedStore_DelegatesToEmbed() {
        MessageBO messageBO = MessageBO.builder().topic(TEST_TOPIC).build();
        EnqueueRequest request = EnqueueRequest.builder().messageBO(messageBO).build();
        EnqueueResult expected = EnqueueResult.notAvailable();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.enqueueAsync(request)).thenReturn(CompletableFuture.completedFuture(expected));

        CompletableFuture<EnqueueResult> future = store.enqueueAsync(request);

        assertTrue(future.isDone());
        assertEquals(expected, future.join());
        verify(embedMQStore).enqueueAsync(request);
        verify(remoteMQStore, never()).enqueueAsync(any());
    }

    @Test
    void testEnqueueAsync_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        MessageBO messageBO = MessageBO.builder().topic(TEST_TOPIC).build();
        EnqueueRequest request = EnqueueRequest.builder().messageBO(messageBO).build();
        EnqueueResult expected = EnqueueResult.notAvailable();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.enqueueAsync(request)).thenReturn(CompletableFuture.completedFuture(expected));

        CompletableFuture<EnqueueResult> future = store.enqueueAsync(request);

        assertTrue(future.isDone());
        assertEquals(expected, future.join());
        verify(remoteMQStore).enqueueAsync(request);
    }

    @Test
    void testEnqueueAsync_TopicNotInEmbed_RemoteDisabled_ReturnsNotAvailable() {
        MessageBO messageBO = MessageBO.builder().topic(TEST_TOPIC).build();
        EnqueueRequest request = EnqueueRequest.builder().messageBO(messageBO).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        CompletableFuture<EnqueueResult> future = store.enqueueAsync(request);

        assertTrue(future.isDone());
        assertFalse(future.join().isSuccess());
        verify(remoteMQStore, never()).enqueueAsync(any());
        verify(embedMQStore, never()).enqueueAsync(any());
    }

    // ==================== dequeue ====================

    @Test
    void testDequeue_TopicInEmbedStore_DelegatesToEmbed() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();
        DequeueResult expected = DequeueResult.notFound();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.dequeue(request)).thenReturn(expected);

        DequeueResult result = store.dequeue(request);

        assertEquals(expected, result);
        verify(embedMQStore).dequeue(request);
        verify(remoteMQStore, never()).dequeue(any());
    }

    @Test
    void testDequeue_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();
        DequeueResult expected = DequeueResult.notFound();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.dequeue(request)).thenReturn(expected);

        DequeueResult result = store.dequeue(request);

        assertEquals(expected, result);
        verify(remoteMQStore).dequeue(request);
    }

    @Test
    void testDequeue_TopicNotInEmbed_RemoteDisabled_ReturnsNotFound() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        DequeueResult result = store.dequeue(request);

        assertTrue(result.isEmpty());
        verify(remoteMQStore, never()).dequeue(any());
        verify(embedMQStore, never()).dequeue(any());
    }

    // ==================== dequeueAsync ====================

    @Test
    void testDequeueAsync_TopicInEmbedStore_DelegatesToEmbed() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();
        DequeueResult expected = DequeueResult.notFound();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.dequeueAsync(request)).thenReturn(CompletableFuture.completedFuture(expected));

        CompletableFuture<DequeueResult> future = store.dequeueAsync(request);

        assertTrue(future.isDone());
        assertEquals(expected, future.join());
        verify(embedMQStore).dequeueAsync(request);
        verify(remoteMQStore, never()).dequeueAsync(any());
    }

    @Test
    void testDequeueAsync_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();
        DequeueResult expected = DequeueResult.notFound();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.dequeueAsync(request)).thenReturn(CompletableFuture.completedFuture(expected));

        CompletableFuture<DequeueResult> future = store.dequeueAsync(request);

        assertTrue(future.isDone());
        assertEquals(expected, future.join());
        verify(remoteMQStore).dequeueAsync(request);
    }

    @Test
    void testDequeueAsync_TopicNotInEmbed_RemoteDisabled_ReturnsNotFound() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        CompletableFuture<DequeueResult> future = store.dequeueAsync(request);

        assertTrue(future.isDone());
        assertTrue(future.join().isEmpty());
        verify(remoteMQStore, never()).dequeueAsync(any());
        verify(embedMQStore, never()).dequeueAsync(any());
    }

    // ==================== get ====================

    @Test
    void testGet_TopicInEmbedStore_DelegatesToEmbed() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();
        DequeueResult expected = DequeueResult.notFound();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.get(request)).thenReturn(expected);

        DequeueResult result = store.get(request);

        assertEquals(expected, result);
        verify(embedMQStore).get(request);
        verify(remoteMQStore, never()).get(any());
    }

    @Test
    void testGet_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();
        DequeueResult expected = DequeueResult.notFound();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.get(request)).thenReturn(expected);

        DequeueResult result = store.get(request);

        assertEquals(expected, result);
        verify(remoteMQStore).get(request);
    }

    @Test
    void testGet_TopicNotInEmbed_RemoteDisabled_ReturnsNotFound() {
        DequeueRequest request = DequeueRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        DequeueResult result = store.get(request);

        assertTrue(result.isEmpty());
        verify(remoteMQStore, never()).get(any());
        verify(embedMQStore, never()).get(any());
    }

    // ==================== getMessage ====================

    @Test
    void testGetMessage_StoreGroupBlank_DelegatesToEmbed() {
        MessageRequest request = MessageRequest.builder().storeGroup("").build();
        MessageResult expected = MessageResult.notFound();

        when(embedMQStore.getMessage(request)).thenReturn(expected);

        MessageResult result = store.getMessage(request);

        assertEquals(expected, result);
        verify(embedMQStore).getMessage(request);
    }

    @Test
    void testGetMessage_StoreGroupNotBlank_RemoteEnabled_DelegatesToEmbed() {
        MessageRequest request = MessageRequest.builder().storeGroup("test_group").build();
        MessageResult expected = MessageResult.notFound();

        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(embedMQStore.getMessage(request)).thenReturn(expected);

        MessageResult result = store.getMessage(request);

        assertEquals(expected, result);
        verify(embedMQStore).getMessage(request);
    }

    @Test
    void testGetMessage_StoreGroupNotBlank_RemoteDisabled_ReturnsNotFound() {
        MessageRequest request = MessageRequest.builder().storeGroup("test_group").build();

        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        MessageResult result = store.getMessage(request);

        assertFalse(result.isSuccess());
        verify(embedMQStore, never()).getMessage(any());
        verify(remoteMQStore, never()).getMessage(any());
    }

    // ==================== addCheckPoint ====================

    @Test
    void testAddCheckPoint_TopicInEmbedStore_DelegatesToEmbed() {
        PopCheckPoint checkPoint = PopCheckPoint.builder().topic(TEST_TOPIC).build();
        CheckPointRequest request = new CheckPointRequest();
        request.setCheckPoint(checkPoint);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);

        store.addCheckPoint(request);

        verify(embedMQStore).addCheckPoint(request);
        verify(remoteMQStore, never()).addCheckPoint(any());
    }

    @Test
    void testAddCheckPoint_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        PopCheckPoint checkPoint = PopCheckPoint.builder().topic(TEST_TOPIC).build();
        CheckPointRequest request = new CheckPointRequest();
        request.setCheckPoint(checkPoint);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);

        store.addCheckPoint(request);

        verify(remoteMQStore).addCheckPoint(request);
        verify(embedMQStore, never()).addCheckPoint(any());
    }

    @Test
    void testAddCheckPoint_TopicNotInEmbed_RemoteDisabled_DoesNothing() {
        PopCheckPoint checkPoint = PopCheckPoint.builder().topic(TEST_TOPIC).build();
        CheckPointRequest request = new CheckPointRequest();
        request.setCheckPoint(checkPoint);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        // Should not throw
        assertDoesNotThrow(() -> store.addCheckPoint(request));

        verify(remoteMQStore, never()).addCheckPoint(any());
        verify(embedMQStore, never()).addCheckPoint(any());
    }

    // ==================== ack ====================

    @Test
    void testAck_TopicInEmbedStore_DelegatesToEmbed() {
        AckInfo ackInfo = AckInfo.builder().topic(TEST_TOPIC).build();
        AckMessage request = AckMessage.builder().ackInfo(ackInfo).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);

        store.ack(request);

        verify(embedMQStore).ack(request);
        verify(remoteMQStore, never()).ack(any());
    }

    @Test
    void testAck_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        AckInfo ackInfo = AckInfo.builder().topic(TEST_TOPIC).build();
        AckMessage request = AckMessage.builder().ackInfo(ackInfo).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);

        store.ack(request);

        verify(remoteMQStore).ack(request);
        verify(embedMQStore, never()).ack(any());
    }

    @Test
    void testAck_TopicNotInEmbed_RemoteDisabled_DoesNothing() {
        AckInfo ackInfo = AckInfo.builder().topic(TEST_TOPIC).build();
        AckMessage request = AckMessage.builder().ackInfo(ackInfo).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        // Should not throw
        assertDoesNotThrow(() -> store.ack(request));

        verify(remoteMQStore, never()).ack(any());
        verify(embedMQStore, never()).ack(any());
    }

    // ==================== changeInvisible ====================

    @Test
    void testChangeInvisible_TopicInEmbedStore_DelegatesToEmbed() {
        AckInfo ackInfo = AckInfo.builder().topic(TEST_TOPIC).build();
        AckMessage request = AckMessage.builder().ackInfo(ackInfo).build();
        AckResult expected = AckResult.success();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.changeInvisible(request)).thenReturn(expected);

        AckResult result = store.changeInvisible(request);

        assertEquals(expected, result);
        verify(embedMQStore).changeInvisible(request);
        verify(remoteMQStore, never()).changeInvisible(any());
    }

    @Test
    void testChangeInvisible_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        AckInfo ackInfo = AckInfo.builder().topic(TEST_TOPIC).build();
        AckMessage request = AckMessage.builder().ackInfo(ackInfo).build();
        AckResult expected = AckResult.success();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.changeInvisible(request)).thenReturn(expected);

        AckResult result = store.changeInvisible(request);

        assertEquals(expected, result);
        verify(remoteMQStore).changeInvisible(request);
    }

    @Test
    void testChangeInvisible_TopicNotInEmbed_RemoteDisabled_ReturnsFailure() {
        AckInfo ackInfo = AckInfo.builder().topic(TEST_TOPIC).build();
        AckMessage request = AckMessage.builder().ackInfo(ackInfo).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        AckResult result = store.changeInvisible(request);

        assertFalse(result.isSuccess());
        verify(remoteMQStore, never()).changeInvisible(any());
        verify(embedMQStore, never()).changeInvisible(any());
    }

    // ==================== getBufferedOffset ====================

    @Test
    void testGetBufferedOffset_TopicInEmbedStore_DelegatesToEmbed() {
        OffsetRequest request = OffsetRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.getBufferedOffset(request)).thenReturn(42L);

        long result = store.getBufferedOffset(request);

        assertEquals(42L, result);
        verify(embedMQStore).getBufferedOffset(request);
        verify(remoteMQStore, never()).getBufferedOffset(any());
    }

    @Test
    void testGetBufferedOffset_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        OffsetRequest request = OffsetRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.getBufferedOffset(request)).thenReturn(99L);

        long result = store.getBufferedOffset(request);

        assertEquals(99L, result);
        verify(remoteMQStore).getBufferedOffset(request);
    }

    @Test
    void testGetBufferedOffset_TopicNotInEmbed_RemoteDisabled_ReturnsZero() {
        OffsetRequest request = OffsetRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        long result = store.getBufferedOffset(request);

        assertEquals(0L, result);
        verify(remoteMQStore, never()).getBufferedOffset(any());
        verify(embedMQStore, never()).getBufferedOffset(any());
    }

    // ==================== getMinOffset ====================

    @Test
    void testGetMinOffset_TopicInEmbedStore_DelegatesToEmbed() {
        QueueRequest request = QueueRequest.builder().topicName(TEST_TOPIC).build();
        QueueResult expected = QueueResult.minOffset(10L);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.getMinOffset(request)).thenReturn(expected);

        QueueResult result = store.getMinOffset(request);

        assertEquals(10L, result.getMinOffset());
        verify(embedMQStore).getMinOffset(request);
        verify(remoteMQStore, never()).getMinOffset(any());
    }

    @Test
    void testGetMinOffset_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        QueueRequest request = QueueRequest.builder().topicName(TEST_TOPIC).build();
        QueueResult expected = QueueResult.minOffset(20L);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.getMinOffset(request)).thenReturn(expected);

        QueueResult result = store.getMinOffset(request);

        assertEquals(20L, result.getMinOffset());
        verify(remoteMQStore).getMinOffset(request);
    }

    @Test
    void testGetMinOffset_TopicNotInEmbed_RemoteDisabled_ReturnsMinOffsetZero() {
        QueueRequest request = QueueRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        QueueResult result = store.getMinOffset(request);

        assertEquals(0L, result.getMinOffset());
        verify(remoteMQStore, never()).getMinOffset(any());
        verify(embedMQStore, never()).getMinOffset(any());
    }

    // ==================== getMaxOffset ====================

    @Test
    void testGetMaxOffset_TopicInEmbedStore_DelegatesToEmbed() {
        QueueRequest request = QueueRequest.builder().topicName(TEST_TOPIC).build();
        QueueResult expected = QueueResult.maxOffset(100L);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(true);
        when(embedMQStore.getMaxOffset(request)).thenReturn(expected);

        QueueResult result = store.getMaxOffset(request);

        assertEquals(100L, result.getMaxOffset());
        verify(embedMQStore).getMaxOffset(request);
        verify(remoteMQStore, never()).getMaxOffset(any());
    }

    @Test
    void testGetMaxOffset_TopicNotInEmbed_RemoteEnabled_DelegatesToRemote() {
        QueueRequest request = QueueRequest.builder().topicName(TEST_TOPIC).build();
        QueueResult expected = QueueResult.maxOffset(200L);

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteMQStore.getMaxOffset(request)).thenReturn(expected);

        QueueResult result = store.getMaxOffset(request);

        assertEquals(200L, result.getMaxOffset());
        verify(remoteMQStore).getMaxOffset(request);
    }

    @Test
    void testGetMaxOffset_TopicNotInEmbed_RemoteDisabled_ReturnsMaxOffsetZero() {
        QueueRequest request = QueueRequest.builder().topicName(TEST_TOPIC).build();

        when(embedMQStore.containsTopic(TEST_TOPIC)).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        QueueResult result = store.getMaxOffset(request);

        assertEquals(0L, result.getMaxOffset());
        verify(remoteMQStore, never()).getMaxOffset(any());
        verify(embedMQStore, never()).getMaxOffset(any());
    }
}
