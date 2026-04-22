package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IndexCommitHandlerTest {

    @Test
    void testImplementsCommitHandler() {
        IndexCommitHandler handler = new IndexCommitHandler();
        assertInstanceOf(cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitHandler.class, handler);
    }

    @Test
    void testHandleDoesNotThrow() {
        IndexCommitHandler handler = new IndexCommitHandler();
        CommitEvent event = mock(CommitEvent.class);
        assertDoesNotThrow(() -> handler.handle(event));
    }
}