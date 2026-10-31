package com.wolfmq.store.domain.mq.queue;

import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.message.enums.MessageStatus;
import com.wolfmq.domain.domain.consumequeue.QueueUnit;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.test.MessageMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    @Test
    void getMessageReturnsNotFoundWhenInvalid() {
        CommitLogProxy commitLog = mock(CommitLogProxy.class);
        ConsumeQueueProxy consumeQueueProxy = mock(ConsumeQueueProxy.class);
        MessageService service = new MessageService(commitLog, consumeQueueProxy);

        MessageBO invalid = MessageBO.notFound();
        when(commitLog.select(7L)).thenReturn(invalid);

        MessageResult result = service.getMessage(MessageRequest.builder().offset(7L).build());
        assertFalse(result.isSuccess());
        assertEquals(invalid.getStatus(), result.getStatus());
    }

    @Test
    void getSkipsNullMessagesAndSetsStatus() {
        CommitLogProxy commitLog = mock(CommitLogProxy.class);
        ConsumeQueueProxy consumeQueueProxy = mock(ConsumeQueueProxy.class);
        MessageService service = new MessageService(commitLog, consumeQueueProxy);

        QueueUnit first = QueueUnit.builder()
            .commitOffset(10)
            .messageSize(5)
            .build();
        QueueUnit second = QueueUnit.builder()
            .commitOffset(20)
            .messageSize(7)
            .build();

        when(consumeQueueProxy.get("TOPIC_A", 0, 1, 2)).thenReturn(List.of(first, second));

        MessageBO message = MessageMock.createMessage("TOPIC_A", 0, 1);
        message.setStatus(MessageStatus.FOUND);
        when(commitLog.select(10L, 5)).thenReturn(message);
        when(commitLog.select(20L, 7)).thenReturn(null);

        DequeueResult result = service.get("TOPIC_A", 0, 1, 2);

        assertEquals(1, result.countMessage());
        assertTrue(result.getMessageList().contains(message));
        assertEquals(MessageStatus.FOUND, result.getStatus());
    }
}
