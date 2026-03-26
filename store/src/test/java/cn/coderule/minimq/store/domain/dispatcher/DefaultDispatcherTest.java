package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitHandler;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import cn.coderule.minimq.domain.test.MessageMock;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DefaultDispatcherTest {

    @Test
    void dispatchCallsRegisteredHandlers() {
        CommitLog commitLog = mock(CommitLog.class);
        CheckPoint checkPoint = mock(CheckPoint.class);
        DefaultCommitEventDispatcher dispatcher = new DefaultCommitEventDispatcher(commitLog, checkPoint);

        CommitHandler handler = mock(CommitHandler.class);
        dispatcher.registerHandler(handler);

        MessageBO message = MessageMock.createMessage("TOPIC_A", 0, 1);
        CommitEvent event = CommitEvent.of(message);

        dispatcher.dispatch(event);

        verify(handler).handle(event);
    }

    @Test
    void dispatchDoesNothingWithoutHandlers() {
        CommitLog commitLog = mock(CommitLog.class);
        CheckPoint checkPoint = mock(CheckPoint.class);
        DefaultCommitEventDispatcher dispatcher = new DefaultCommitEventDispatcher(commitLog, checkPoint);

        CommitHandler handler = mock(CommitHandler.class);
        CommitEvent event = CommitEvent.of(MessageMock.createMessage("TOPIC_B", 0, 1));

        dispatcher.dispatch(event);

        verifyNoInteractions(handler);
    }
}
