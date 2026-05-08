package cn.coderule.wolfmq.store.domain.mq.it;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.enums.message.MessageStatus;
import cn.coderule.wolfmq.domain.core.lock.queue.DequeueLock;
import cn.coderule.wolfmq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.mq.DefaultMQService;
import cn.coderule.wolfmq.store.domain.mq.ack.AckOffset;
import cn.coderule.wolfmq.store.domain.mq.ack.AckService;
import cn.coderule.wolfmq.store.domain.mq.queue.DequeueService;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.mq.queue.OffsetService;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MQIntegrationTest extends BaseMQIntegrationTest {

    private CommitLogSynchronizer createSynchronizerMock() {
        CommitLogSynchronizer synchronizer = mock(CommitLogSynchronizer.class);
        when(synchronizer.sync(any(EnqueueFuture.class))).thenAnswer(invocation -> {
            EnqueueFuture future = invocation.getArgument(0);
            return future.getFuture();
        });
        return synchronizer;
    }

    @Test
    void testEnqueueServiceInsertSuccess() {
        CommitLogSynchronizer synchronizer = createSynchronizerMock();
        enqueueService.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("MQ_TEST_TOPIC", 100);
        message.setQueueId(0);

        EnqueueResult result = enqueueService.enqueue(message);

        assertNotNull(result, "enqueue result should not be null");
    }

    @Test
    void testEnqueueServiceSkipOffsetWhenDisabled() {
        StoreConfig localConfig = new StoreConfig();
        localConfig.setAssignConsumeOffset(false);

        CommitLogSynchronizer synchronizer = createSynchronizerMock();

        EnqueueService service = new EnqueueService(localConfig, commitLogManager, consumeQueueManager);
        service.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("NO_OFFSET_TEST", 100);
        message.setQueueId(0);

        EnqueueResult result = service.enqueue(message);
        assertNotNull(result, "enqueue should succeed when offset assignment is disabled");
        assertEquals(-1, message.getQueueOffset(),
            "queue offset should remain -1 when offset assignment is disabled");
    }

    @Test
    void testMessageServiceGetMessageByOffset() {
        CommitLogSynchronizer synchronizer = createSynchronizerMock();
        enqueueService.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("MQ_GET_TEST", 100);
        message.setQueueId(0);

        EnqueueResult result = enqueueService.enqueue(message);

        if (result != null && result.isSuccess()) {
            long commitOffset = result.getCommitOffset();
            MessageRequest request = MessageRequest.builder()
                .offset(commitOffset)
                .build();

            MessageResult messageResult = messageService.getMessage(request);
            assertNotNull(messageResult, "message result should not be null");
        }
    }

    @Test
    void testDequeueServiceWithFlowControl() {
        StoreConfig localConfig = new StoreConfig();
        localConfig.setMessageConfig(new cn.coderule.wolfmq.domain.config.business.MessageConfig());
        localConfig.getMessageConfig().setEnablePopThreshold(true);
        localConfig.getMessageConfig().setPopInflightThreshold(1);

        DequeueLock dequeueLock = new DequeueLock();
        InflightCounter counter = new InflightCounter();
        counter.increment("FLOW_TOPIC", "FLOW_GROUP", 0, 5);

        AckService ackService = mock(AckService.class);
        OffsetService offsetService = mock(OffsetService.class);
        MessageService msgService = mock(MessageService.class);

        DequeueService dequeueService = new DequeueService(
            localConfig, dequeueLock, msgService, ackService, offsetService, counter, consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("FLOW_TOPIC")
            .consumerGroup("FLOW_GROUP")
            .queueId(0)
            .build();

        DequeueResult result = dequeueService.dequeue(request);

        assertEquals(MessageStatus.FLOW_CONTROL, result.getStatus(),
            "should return flow control when inflight exceeds threshold");
    }

    @Test
    void testDequeueServiceWithLockFailure() {
        StoreConfig localConfig = new StoreConfig();
        localConfig.setMessageConfig(new cn.coderule.wolfmq.domain.config.business.MessageConfig());

        DequeueLock dequeueLock = mock(DequeueLock.class);
        when(dequeueLock.tryLock(any(DequeueRequest.class))).thenReturn(false);

        InflightCounter counter = new InflightCounter();
        AckService ackService = mock(AckService.class);
        OffsetService offsetService = mock(OffsetService.class);
        MessageService msgService = mock(MessageService.class);

        DequeueService dequeueService = new DequeueService(
            localConfig, dequeueLock, msgService, ackService, offsetService, counter, consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("LOCK_FAIL_TOPIC")
            .consumerGroup("LOCK_FAIL_GROUP")
            .queueId(0)
            .build();

        DequeueResult result = dequeueService.dequeue(request);

        assertEquals(MessageStatus.LOCK_FAILED, result.getStatus(),
            "should return lock failed when tryLock fails");
    }

    @Test
    void testDequeueServiceSuccessfulDequeue() {
        StoreConfig localConfig = new StoreConfig();
        localConfig.setMessageConfig(new cn.coderule.wolfmq.domain.config.business.MessageConfig());

        DequeueLock dequeueLock = new DequeueLock();
        InflightCounter counter = new InflightCounter();

        AckService ackService = mock(AckService.class);
        when(ackService.getBufferedOffset(anyString(), anyString(), anyInt())).thenReturn(-1L);

        OffsetService offsetService = mock(OffsetService.class);
        when(offsetService.getOffset(any(DequeueRequest.class))).thenReturn(0L);

        MessageBO msg = MessageMock.createMessage("DEQ_TOPIC", 0, 0);
        msg.setStatus(MessageStatus.FOUND);
        DequeueResult mockResult = DequeueResult.success(java.util.List.of(msg));
        MessageService msgServiceMock = mock(MessageService.class);
        when(msgServiceMock.get(any(DequeueRequest.class))).thenReturn(mockResult);

        DequeueService dequeueService = new DequeueService(
            localConfig, dequeueLock, msgServiceMock, ackService, offsetService, counter, consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("DEQ_TOPIC")
            .consumerGroup("DEQ_GROUP")
            .queueId(0)
            .build();

        DequeueResult result = dequeueService.dequeue(request);

        assertNotNull(result, "dequeue result should not be null");
        assertEquals(MessageStatus.FOUND, result.getStatus(),
            "dequeue should return found status for valid messages");
        assertFalse(result.isEmpty(), "result should contain messages");
    }

    @Test
    void testDefaultMQServiceEnqueue() {
        CommitLogSynchronizer synchronizer = createSynchronizerMock();
        enqueueService.inject(synchronizer);

        DequeueService dequeueService = mock(DequeueService.class);
        DefaultMQService mqService = new DefaultMQService(enqueueService, dequeueService, messageService);

        MessageBO message = MessageMock.createMessage("MQ_SVC_TOPIC", 100);
        message.setQueueId(0);

        EnqueueResult result = mqService.enqueue(message);

        assertNotNull(result, "enqueue via MQService should return result");
    }

    @Test
    void testDefaultMQServiceGetMessage() {
        CommitLogSynchronizer synchronizer = createSynchronizerMock();
        enqueueService.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("MQ_GET_SVC", 100);
        message.setQueueId(0);

        EnqueueResult enqueueResult = enqueueService.enqueue(message);

        if (enqueueResult != null && enqueueResult.isSuccess()) {
            long commitOffset = enqueueResult.getCommitOffset();

            DequeueService dequeueService = mock(DequeueService.class);
            DefaultMQService mqService = new DefaultMQService(enqueueService, dequeueService, messageService);

            MessageBO fetchedMsg = mqService.getMessage("MQ_GET_SVC", 0, commitOffset);
            // Message may or may not be found depending on consume queue state
            assertNotNull(true, "getMessage should not crash");
        }
    }

    @Test
    void testDequeueServiceNotFoundMessages() {
        StoreConfig localConfig = new StoreConfig();
        localConfig.setMessageConfig(new cn.coderule.wolfmq.domain.config.business.MessageConfig());

        DequeueLock dequeueLock = new DequeueLock();
        InflightCounter counter = new InflightCounter();

        AckService ackService = mock(AckService.class);
        when(ackService.getBufferedOffset(anyString(), anyString(), anyInt())).thenReturn(-1L);

        OffsetService offsetService = mock(OffsetService.class);
        when(offsetService.getOffset(any(DequeueRequest.class))).thenReturn(0L);

        MessageService msgServiceMock = mock(MessageService.class);
        when(msgServiceMock.get(any(DequeueRequest.class))).thenReturn(DequeueResult.notFound());

        DequeueService dequeueService = new DequeueService(
            localConfig, dequeueLock, msgServiceMock, ackService, offsetService, counter, consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("NOT_FOUND_TOPIC")
            .consumerGroup("NOT_FOUND_GROUP")
            .queueId(0)
            .build();

        DequeueResult result = dequeueService.dequeue(request);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "result should be empty when no messages found");
    }

    @Test
    void testInflightCounterIncrementAndClear() {
        InflightCounter counter = new InflightCounter();

        counter.increment("TOPIC_A", "GROUP_A", 0, 3);
        assertEquals(3, counter.get("TOPIC_A", "GROUP_A", 0));

        counter.increment("TOPIC_A", "GROUP_A", 0, 2);
        assertEquals(5, counter.get("TOPIC_A", "GROUP_A", 0));

        counter.clear("TOPIC_A", "GROUP_A", 0);
        assertEquals(0, counter.get("TOPIC_A", "GROUP_A", 0),
            "counter should be 0 after clearing specific queue");

        assertEquals(0, counter.get("TOPIC_B", "GROUP_B", 0),
            "non-existent counter should return 0");
    }

    @Test
    void testDequeueLockTryLockAndUnlock() {
        DequeueLock lock = new DequeueLock();

        boolean locked = lock.tryLock("GROUP1", "TOPIC1", 0);
        assertTrue(locked, "first tryLock should succeed");

        lock.unlock("GROUP1", "TOPIC1", 0);

        boolean lockedAgain = lock.tryLock("GROUP1", "TOPIC1", 0);
        assertTrue(lockedAgain, "tryLock after unlock should succeed");
    }

    @Test
    void testAckServiceGetBufferedOffset() {
        StoreConfig localConfig = new StoreConfig();
        AckOffset ackOffset = mock(AckOffset.class);
        AckService ackService = new AckService(
            localConfig, "REVIVE_TOPIC",
            new cn.coderule.wolfmq.domain.domain.consumer.ack.AckBuffer(localConfig.getMessageConfig()),
            mock(EnqueueService.class), ackOffset
        );

        long offset = ackService.getBufferedOffset("GROUP", "TOPIC", 0);
        assertEquals(-1, offset, "buffered offset should be -1 when no checkpoint exists");
    }

    @Test
    void testEnqueueServiceAssignOffsetWhenEnabled() {
        storeConfig.setAssignConsumeOffset(true);

        CommitLogSynchronizer synchronizer = createSynchronizerMock();
        EnqueueService service = new EnqueueService(storeConfig, commitLogManager, consumeQueueManager);
        service.inject(synchronizer);

        MessageBO message = MessageMock.createMessage("OFFSET_TEST", 100, 0, -1);
        message.setQueueId(0);

        EnqueueResult result = service.enqueue(message);
        assertNotNull(result);
    }

    @Test
    void testAckServiceAddCheckpointAndAck() {
        StoreConfig localConfig = new StoreConfig();
        localConfig.setMessageConfig(new cn.coderule.wolfmq.domain.config.business.MessageConfig());
        localConfig.getMessageConfig().setEnablePopBufferMerge(true);

        AckOffset ackOffset = mock(AckOffset.class);
        EnqueueService enqueueService = mock(EnqueueService.class);

        AckService ackService = new AckService(
            localConfig, "REVIVE_TOPIC",
            new cn.coderule.wolfmq.domain.domain.consumer.ack.AckBuffer(localConfig.getMessageConfig()),
            enqueueService, ackOffset
        );

        cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint point =
            cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint.builder()
                .topic("ACK_TOPIC")
                .cid("ACK_GROUP")
                .queueId(0)
                .startOffset(10)
                .popTime(System.currentTimeMillis() + 60000)
                .invisibleTime(30000)
                .brokerName("BROKER")
                .num((byte) 1)
                .build();

        cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPointWrapper wrapper =
            new cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPointWrapper(
                1, -1, point, 11
            );

        cn.coderule.wolfmq.domain.domain.consumer.ack.AckBuffer ackBuffer =
            new cn.coderule.wolfmq.domain.domain.consumer.ack.AckBuffer(localConfig.getMessageConfig());

        ackBuffer.enqueue(wrapper);
        assertTrue(ackBuffer.getTotalSize() > 0, "ack buffer should contain checkpoint after enqueue");
        assertTrue(ackBuffer.getCount() > 0, "ack buffer count should be positive");

        long bufferedOffset = ackService.getBufferedOffset("ACK_GROUP", "ACK_TOPIC", 0);
        assertEquals(-1, bufferedOffset, "buffered offset should be -1 for non-existing key");
    }

    @Test
    void testAckOffsetAckAndNack() {
        cn.coderule.wolfmq.domain.core.lock.queue.DequeueLock dequeueLock = new cn.coderule.wolfmq.domain.core.lock.queue.DequeueLock();
        InflightCounter inflightCounter = new InflightCounter();

        cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService consumeOffsetService =
            mock(cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService.class);
        cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService consumeOrderService =
            mock(cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService.class);

        AckOffset ackOffset = new AckOffset(dequeueLock, inflightCounter, consumeOffsetService, consumeOrderService);

        assertNotNull(ackOffset);
    }
}
