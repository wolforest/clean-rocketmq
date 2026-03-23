package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.store.domain.consumequeue.ConsumeQueueManager;
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
        CommitLog commitLog = mock(CommitLog.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        MessageService service = new MessageService(commitLog, consumeQueueManager);

        MessageBO invalid = MessageBO.notFound();
        when(commitLog.select(7L)).thenReturn(invalid);

        MessageResult result = service.getMessage(MessageRequest.builder().offset(7L).build());
        assertFalse(result.isSuccess());
        assertEquals(invalid.getStatus(), result.getStatus());
    }

    @Test
    void getSkipsNullMessagesAndSetsStatus() {
        CommitLog commitLog = mock(CommitLog.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        MessageService service = new MessageService(commitLog, consumeQueueManager);

        QueueUnit first = QueueUnit.builder()
            .commitOffset(10)
            .messageSize(5)
            .build();
        QueueUnit second = QueueUnit.builder()
            .commitOffset(20)
            .messageSize(7)
            .build();

        when(consumeQueueManager.get("TOPIC_A", 0, 1, 2)).thenReturn(List.of(first, second));

        MessageBO message = MessageMock.createMessage("TOPIC_A", 0, 1);
        message.setStatus(cn.coderule.minimq.domain.core.enums.message.MessageStatus.FOUND);
        when(commitLog.select(10L, 5)).thenReturn(message);
        when(commitLog.select(20L, 7)).thenReturn(null);

        DequeueResult result = service.get("TOPIC_A", 0, 1, 2);

        assertEquals(1, result.countMessage());
        assertTrue(result.getMessageList().contains(message));
        assertEquals(cn.coderule.minimq.domain.core.enums.message.MessageStatus.FOUND, result.getStatus());
    }
}
