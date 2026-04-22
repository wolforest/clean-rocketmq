package cn.coderule.wolfmq.store.domain.dispatcher;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitHandler;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultCommitEventDispatcherTest {

    private CommitLog commitLog;
    private CheckPoint checkPoint;
    private DefaultCommitEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        commitLog = mock(CommitLog.class);
        checkPoint = mock(CheckPoint.class);
        dispatcher = new DefaultCommitEventDispatcher(commitLog, checkPoint);
    }

    @Test
    void testGetServiceName() {
        assertEquals("DefaultCommitEventDispatcher", dispatcher.getServiceName());
    }

    @Test
    void testRegisterHandler() {
        CommitHandler handler = mock(CommitHandler.class);
        
        dispatcher.registerHandler(handler);
        
        CommitEvent event = mock(CommitEvent.class);
        dispatcher.dispatch(event);
        
        verify(handler).handle(event);
    }

    @Test
    void testDispatchWithNoHandlers() {
        CommitEvent event = mock(CommitEvent.class);
        
        assertDoesNotThrow(() -> dispatcher.dispatch(event));
    }

    @Test
    void testDispatchWithMultipleHandlers() {
        CommitHandler handler1 = mock(CommitHandler.class);
        CommitHandler handler2 = mock(CommitHandler.class);
        
        dispatcher.registerHandler(handler1);
        dispatcher.registerHandler(handler2);
        
        CommitEvent event = mock(CommitEvent.class);
        dispatcher.dispatch(event);
        
        verify(handler1).handle(event);
        verify(handler2).handle(event);
    }

    @Test
    void testSetAndGetDispatchedOffset() {
        dispatcher.setDispatchedOffset(100L);
        
        assertEquals(100L, dispatcher.getDispatchedOffset());
    }

    @Test
    void testInitialDispatchedOffset() {
        assertEquals(-1L, dispatcher.getDispatchedOffset());
    }
}