package cn.coderule.wolfmq.store.domain.mq.queue;

import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.store.domain.mq.ack.AckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

@SuppressWarnings("deprecation")
public class CheckpointServiceTest {

    private AckService ackService;
    private CheckpointService checkpointService;

    @BeforeEach
    void setUp() {
        ackService = mock(AckService.class);
        checkpointService = new CheckpointService(ackService);
    }

    @Test
    void testAddCallsAckService() {
        DequeueRequest request = mock(DequeueRequest.class);
        DequeueResult result = mock(DequeueResult.class);
        MessageBO messageBO = mock(MessageBO.class);

        when(request.getReviveQueueId()).thenReturn(1);
        when(result.getNextOffset()).thenReturn(100L);
        when(result.getFirstMessage()).thenReturn(messageBO);
        when(messageBO.getQueueOffset()).thenReturn(50L);
        when(request.getTopicName()).thenReturn("TOPIC_A");
        when(request.getConsumerGroup()).thenReturn("GROUP_A");
        when(request.getQueueId()).thenReturn(0);
        when(request.getDequeueTime()).thenReturn(System.currentTimeMillis());
        when(request.getInvisibleTime()).thenReturn(30000L);
        when(result.countMessage()).thenReturn(1);
        when(result.getOffsetList()).thenReturn(Collections.singletonList(50L));

        checkpointService.add(request, result);

        verify(ackService).addCheckPoint(any(PopCheckPoint.class), eq(1), eq(-1L), eq(100L));
    }

    @Test
    void testAddWithDifferentParams() {
        DequeueRequest request = mock(DequeueRequest.class);
        DequeueResult result = mock(DequeueResult.class);
        MessageBO messageBO = mock(MessageBO.class);

        when(request.getReviveQueueId()).thenReturn(3);
        when(result.getNextOffset()).thenReturn(200L);
        when(result.getFirstMessage()).thenReturn(messageBO);
        when(messageBO.getQueueOffset()).thenReturn(150L);
        when(request.getTopicName()).thenReturn("TOPIC_B");
        when(request.getConsumerGroup()).thenReturn("GROUP_B");
        when(request.getQueueId()).thenReturn(1);
        when(request.getDequeueTime()).thenReturn(System.currentTimeMillis());
        when(request.getInvisibleTime()).thenReturn(60000L);
        when(result.countMessage()).thenReturn(1);
        when(result.getOffsetList()).thenReturn(Collections.singletonList(150L));

        checkpointService.add(request, result);

        verify(ackService).addCheckPoint(any(PopCheckPoint.class), eq(3), eq(-1L), eq(200L));
    }
}