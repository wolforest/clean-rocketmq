package com.wolfmq.store.domain.index;

import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.index.IndexService;
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
