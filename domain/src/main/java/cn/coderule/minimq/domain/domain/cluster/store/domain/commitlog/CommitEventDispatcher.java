package cn.coderule.minimq.domain.domain.cluster.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;

public interface CommitEventDispatcher {
    long getDispatchedOffset();
    void setDispatchedOffset(long offset);

    void registerHandler(CommitEventHandler handler);
    void dispatch(CommitEvent event);
}
