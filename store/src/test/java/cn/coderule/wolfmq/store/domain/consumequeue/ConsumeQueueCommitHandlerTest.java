package cn.coderule.wolfmq.store.domain.consumequeue;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueCommitHandlerTest {

    @Test
    void testConstructor() {
        ConsumeQueueManager manager = mock(ConsumeQueueManager.class);
        
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(manager);
        
        assertNotNull(handler);
    }

    @Test
    void testHandleNormalMessage() {
        ConsumeQueueManager manager = mock(ConsumeQueueManager.class);
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(manager);
        
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isNormalOrCommitMessage()).thenReturn(true);
        CommitEvent event = mock(CommitEvent.class);
        when(event.getMessageBO()).thenReturn(messageBO);
        
        handler.handle(event);
        
        verify(manager).enqueue(event);
    }

    @Test
    void testHandleSkipNonNormalMessage() {
        ConsumeQueueManager manager = mock(ConsumeQueueManager.class);
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(manager);
        
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isNormalOrCommitMessage()).thenReturn(false);
        CommitEvent event = mock(CommitEvent.class);
        when(event.getMessageBO()).thenReturn(messageBO);
        
        handler.handle(event);
        
        verify(manager, never()).enqueue(any());
    }
}