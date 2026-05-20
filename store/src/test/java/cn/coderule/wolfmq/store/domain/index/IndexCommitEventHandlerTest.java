package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.index.IndexService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class IndexCommitEventHandlerTest {

    @Test
    void handleDoesNotThrow() {
        IndexService indexService = mock(IndexService.class);
        IndexCommitHandler handler = new IndexCommitHandler(indexService);
        CommitEvent event = CommitEvent.builder().build();
        handler.handle(event);
    }
}