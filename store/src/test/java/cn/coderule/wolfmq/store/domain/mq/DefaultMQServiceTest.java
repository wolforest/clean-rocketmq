package cn.coderule.wolfmq.store.domain.mq;

import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.test.MessageMock;
import cn.coderule.wolfmq.store.domain.mq.queue.DequeueService;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultMQServiceTest {

    @Test
    void getMessageReturnsFirstMessage() {
        EnqueueService enqueueService = mock(EnqueueService.class);
        DequeueService dequeueService = mock(DequeueService.class);
        MessageService messageService = mock(MessageService.class);
        DefaultMQService service = new DefaultMQService(enqueueService, dequeueService, messageService);

        MessageBO message = MessageMock.createMessage("TOPIC_A", 0, 5);
        DequeueResult result = DequeueResult.success(List.of(message));
        when(messageService.get(any(DequeueRequest.class))).thenReturn(result);

        MessageBO actual = service.getMessage("TOPIC_A", 0, 5);
        assertEquals(message, actual);

        ArgumentCaptor<DequeueRequest> captor = ArgumentCaptor.forClass(DequeueRequest.class);
        verify(messageService).get(captor.capture());
        DequeueRequest request = captor.getValue();
        assertEquals("TOPIC_A", request.getTopicName());
        assertEquals(0, request.getQueueId());
        assertEquals(5, request.getOffset());
        assertEquals(1, request.getNum());
    }

    @Test
    void getMessageReturnsNullWhenEmpty() {
        EnqueueService enqueueService = mock(EnqueueService.class);
        DequeueService dequeueService = mock(DequeueService.class);
        MessageService messageService = mock(MessageService.class);
        DefaultMQService service = new DefaultMQService(enqueueService, dequeueService, messageService);

        when(messageService.get(any(DequeueRequest.class))).thenReturn(DequeueResult.notFound());

        MessageBO actual = service.getMessage("TOPIC_B", 1, 0);
        assertNull(actual);
    }
}
