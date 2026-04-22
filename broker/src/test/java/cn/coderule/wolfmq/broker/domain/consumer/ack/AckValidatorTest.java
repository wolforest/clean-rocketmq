package cn.coderule.wolfmq.broker.domain.consumer.ack;

import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AckValidatorTest {

    @Mock
    private MQFacade mqStore;

    @Mock
    private TopicFacade topicStore;

    @Mock
    private AckMessage mockAckMessage;

    @Mock
    private ReceiptHandle receiptHandle;

    private AckValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new AckValidator(mqStore, topicStore);
    }

    @Test
    void testValidateValidAck() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("TestTopic")
            .queueId(0)
            .ackOffset(100L)
            .consumerGroup("testGroup")
            .build();

        Topic topic = Topic.builder()
            .topicName("TestTopic")
            .readQueueNums(8)
            .writeQueueNums(8)
            .build();

        when(mockAckMessage.getAckInfo()).thenReturn(ackInfo);
        when(mockAckMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(mockAckMessage.getRequestContext()).thenReturn(null);
        when(mockAckMessage.getReceiptStr()).thenReturn("receipt123");
        when(receiptHandle.isExpired()).thenReturn(false);
        when(topicStore.getTopic("TestTopic")).thenReturn(topic);
        when(mqStore.getMinOffset(any(QueueRequest.class)))
            .thenReturn(QueueResult.builder().minOffset(0L).build());
        when(mqStore.getMaxOffset(any(QueueRequest.class)))
            .thenReturn(QueueResult.builder().maxOffset(200L).build());

        assertDoesNotThrow(() -> validator.validate(mockAckMessage));
    }

    @Test
    void testValidateExpiredReceipt() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("TestTopic")
            .build();

        when(mockAckMessage.getAckInfo()).thenReturn(ackInfo);
        when(mockAckMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(mockAckMessage.getReceiptStr()).thenReturn("expiredReceipt");
        when(receiptHandle.isExpired()).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> validator.validate(mockAckMessage));
    }

    @Test
    void testValidateTopicNotExists() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("NonExistentTopic")
            .build();

        when(mockAckMessage.getAckInfo()).thenReturn(ackInfo);
        when(mockAckMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(receiptHandle.isExpired()).thenReturn(false);
        when(topicStore.getTopic("NonExistentTopic")).thenReturn(null);

        assertThrows(InvalidRequestException.class, () -> validator.validate(mockAckMessage));
    }

    @Test
    void testValidateInvalidQueueId() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("TestTopic")
            .queueId(10)
            .build();

        Topic topic = Topic.builder()
            .topicName("TestTopic")
            .readQueueNums(8)
            .writeQueueNums(8)
            .build();

        when(mockAckMessage.getAckInfo()).thenReturn(ackInfo);
        when(mockAckMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(receiptHandle.isExpired()).thenReturn(false);
        when(topicStore.getTopic("TestTopic")).thenReturn(topic);

        assertThrows(InvalidRequestException.class, () -> validator.validate(mockAckMessage));
    }

    @Test
    void testValidateOffsetOutOfRange() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("TestTopic")
            .queueId(0)
            .ackOffset(500L)
            .consumerGroup("testGroup")
            .build();

        Topic topic = Topic.builder()
            .topicName("TestTopic")
            .readQueueNums(8)
            .writeQueueNums(8)
            .build();

        when(mockAckMessage.getAckInfo()).thenReturn(ackInfo);
        when(mockAckMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(mockAckMessage.getRequestContext()).thenReturn(null);
        when(receiptHandle.isExpired()).thenReturn(false);
        when(topicStore.getTopic("TestTopic")).thenReturn(topic);
        when(mqStore.getMinOffset(any(QueueRequest.class)))
            .thenReturn(QueueResult.builder().minOffset(0L).build());
        when(mqStore.getMaxOffset(any(QueueRequest.class)))
            .thenReturn(QueueResult.builder().maxOffset(100L).build());

        assertThrows(InvalidRequestException.class, () -> validator.validate(mockAckMessage));
    }
}
