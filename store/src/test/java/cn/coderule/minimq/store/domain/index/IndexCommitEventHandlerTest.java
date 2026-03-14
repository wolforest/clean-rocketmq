package cn.coderule.minimq.store.domain.index;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import org.junit.jupiter.api.Test;

class IndexCommitEventHandlerTest {

    @Test
    void handleDoesNotThrow() {
        IndexCommitEventHandler handler = new IndexCommitEventHandler();
        handler.handle(new CommitEvent());
    }
}
