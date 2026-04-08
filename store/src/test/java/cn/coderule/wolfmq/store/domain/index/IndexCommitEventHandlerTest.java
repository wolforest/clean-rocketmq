package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import org.junit.jupiter.api.Test;

class IndexCommitEventHandlerTest {

    @Test
    void handleDoesNotThrow() {
        IndexCommitHandler handler = new IndexCommitHandler();
        handler.handle(new CommitEvent());
    }
}
