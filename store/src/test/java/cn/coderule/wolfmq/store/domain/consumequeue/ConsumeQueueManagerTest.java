package cn.coderule.wolfmq.store.domain.consumequeue;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConsumeQueueManagerTest {

    @Test
    void enqueue_ShouldRouteByTopicAndQueueId() {
        ConsumeQueueFactory factory = mock(ConsumeQueueFactory.class);
        ConsumeQueue queue = mock(ConsumeQueue.class);
        ConsumeQueueManager facade = new ConsumeQueueManager(factory);

        String topic = "TOPIC_A";
        int queueId = 3;
        CommitEvent event = createEvent(topic, queueId);
        when(factory.getOrCreate(topic, queueId)).thenReturn(queue);

        facade.enqueue(event);

        verify(factory).getOrCreate(topic, queueId);
        verify(queue).enqueue(event);
    }

    @Test
    void get_ShouldDelegateToQueue() {
        ConsumeQueueFactory factory = mock(ConsumeQueueFactory.class);
        ConsumeQueue queue = mock(ConsumeQueue.class);
        ConsumeQueueManager facade = new ConsumeQueueManager(factory);

        String topic = "TOPIC_B";
        int queueId = 1;
        long offset = 8;
        QueueUnit expected = QueueUnit.builder().queueOffset(offset).commitOffset(99).messageSize(64).build();
        when(factory.getOrCreate(topic, queueId)).thenReturn(queue);
        when(queue.get(offset)).thenReturn(expected);

        QueueUnit actual = facade.get(topic, queueId, offset);

        assertSame(expected, actual);
        verify(factory).getOrCreate(topic, queueId);
        verify(queue).get(offset);
    }

    @Test
    void getBatch_ShouldDelegateToQueue() {
        ConsumeQueueFactory factory = mock(ConsumeQueueFactory.class);
        ConsumeQueue queue = mock(ConsumeQueue.class);
        ConsumeQueueManager facade = new ConsumeQueueManager(factory);

        String topic = "TOPIC_C";
        int queueId = 5;
        long offset = 2;
        int num = 4;
        List<QueueUnit> expected = List.of(
            QueueUnit.builder().queueOffset(2).build(),
            QueueUnit.builder().queueOffset(3).build()
        );
        when(factory.getOrCreate(topic, queueId)).thenReturn(queue);
        when(queue.get(offset, num)).thenReturn(expected);

        List<QueueUnit> actual = facade.get(topic, queueId, offset, num);

        assertSame(expected, actual);
        verify(factory).getOrCreate(topic, queueId);
        verify(queue).get(offset, num);
    }

    @Test
    void offsetMethods_ShouldDelegateToQueue() {
        ConsumeQueueFactory factory = mock(ConsumeQueueFactory.class);
        ConsumeQueue queue = mock(ConsumeQueue.class);
        ConsumeQueueManager facade = new ConsumeQueueManager(factory);

        String topic = "TOPIC_D";
        int queueId = 0;
        when(factory.getOrCreate(topic, queueId)).thenReturn(queue);
        when(queue.increaseOffset()).thenReturn(10L, 11L);
        when(queue.getMinOffset()).thenReturn(3L);
        when(queue.getMaxOffset()).thenReturn(77L);
        when(queue.rollToOffset(888L)).thenReturn(1000L);

        assertEquals(10, facade.assignOffset(topic, queueId));
        assertEquals(11, facade.increaseOffset(topic, queueId));
        assertEquals(3, facade.getMinOffset(topic, queueId));
        assertEquals(77, facade.getMaxOffset(topic, queueId));
        assertEquals(1000, facade.rollToOffset(topic, queueId, 888));

        verify(factory, times(5)).getOrCreate(topic, queueId);
        verify(queue, times(2)).increaseOffset();
        verify(queue).getMinOffset();
        verify(queue).getMaxOffset();
        verify(queue).rollToOffset(888L);
    }

    @Test
    void existsQueue_ShouldDelegateToFactory() {
        ConsumeQueueFactory factory = mock(ConsumeQueueFactory.class);
        ConsumeQueueManager facade = new ConsumeQueueManager(factory);

        when(factory.exists("TOPIC_E", 2)).thenReturn(true);
        when(factory.exists("TOPIC_E", 9)).thenReturn(false);

        assertTrue(facade.existsQueue("TOPIC_E", 2));
        assertFalse(facade.existsQueue("TOPIC_E", 9));
        verify(factory).exists("TOPIC_E", 2);
        verify(factory).exists("TOPIC_E", 9);
    }

    @Test
    void methods_ShouldUseEventTopicAndQueueId() {
        ConsumeQueueFactory factory = mock(ConsumeQueueFactory.class);
        ConsumeQueue queue = mock(ConsumeQueue.class);
        ConsumeQueueManager facade = new ConsumeQueueManager(factory);

        CommitEvent event = createEvent("TOPIC_F", 6);
        when(factory.getOrCreate("TOPIC_F", 6)).thenReturn(queue);
        when(queue.get(anyLong())).thenReturn(QueueUnit.builder().build());
        when(queue.get(anyLong(), anyInt())).thenReturn(List.of());
        when(queue.increaseOffset()).thenReturn(1L);
        when(queue.getMinOffset()).thenReturn(0L);
        when(queue.getMaxOffset()).thenReturn(0L);
        when(queue.rollToOffset(eq(0L))).thenReturn(0L);

        facade.enqueue(event);
        facade.get("TOPIC_F", 6, 0);
        facade.get("TOPIC_F", 6, 0, 1);
        facade.assignOffset("TOPIC_F", 6);
        facade.getMinOffset("TOPIC_F", 6);
        facade.getMaxOffset("TOPIC_F", 6);
        facade.rollToOffset("TOPIC_F", 6, 0);

        verify(factory, atLeastOnce()).getOrCreate("TOPIC_F", 6);
    }

    private CommitEvent createEvent(String topic, int queueId) {
        return CommitEvent.of(MessageMock.createMessage(topic, 128, queueId, 0));
    }
}
