package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrentCommitEventDispatcher implements CommitEventDispatcher {
    @Override
    public long getDispatchedOffset() {
        return 0;
    }

    @Override
    public void setDispatchedOffset(long offset) {

    }

    @Override
    public void registerHandler(CommitEventHandler handler) {

    }

    @Override
    public void dispatch(CommitEvent event) {

    }
}
