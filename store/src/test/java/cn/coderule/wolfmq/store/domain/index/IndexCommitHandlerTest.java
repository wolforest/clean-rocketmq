package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.index.IndexService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IndexCommitHandlerTest {

    private final IndexService indexService = mock(IndexService.class);
    private final IndexCommitHandler handler = new IndexCommitHandler(indexService);

    @Test
    void testImplementsCommitHandler() {
        assertInstanceOf(cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitHandler.class, handler);
    }

    @Test
    void testHandleWithNullMessageBODoesNotThrow() {
        CommitEvent event = CommitEvent.builder().build();
        assertDoesNotThrow(() -> handler.handle(event));
    }

    @Test
    void testHandleWithNormalMessageBuildsIndex() {
        cn.coderule.wolfmq.domain.domain.message.MessageBO messageBO = cn.coderule.wolfmq.domain.domain.message.MessageBO.builder()
            .topic("testTopic")
            .commitOffset(100L)
            .storeTimestamp(System.currentTimeMillis())
            .build();
        messageBO.setKeys("key1,key2");

        CommitEvent event = CommitEvent.of(messageBO);
        assertDoesNotThrow(() -> handler.handle(event));
        verify(indexService, atLeastOnce()).buildIndex(eq("testTopic"), anyString(), eq(100L), anyLong());
    }
}