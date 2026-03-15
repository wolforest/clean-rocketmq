package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentCommitEventDispatcherTest {

    @Test
    void defaultDispatchedOffsetIsZero() {
        ConcurrentCommitEventDispatcher dispatcher = new ConcurrentCommitEventDispatcher();
        assertEquals(0, dispatcher.getDispatchedOffset());
    }

    @Test
    void noOpMethodsDoNotThrow() {
        ConcurrentCommitEventDispatcher dispatcher = new ConcurrentCommitEventDispatcher();
        dispatcher.setDispatchedOffset(10);
        dispatcher.registerHandler(event -> {});
        dispatcher.dispatch(new CommitEvent());
    }
}
