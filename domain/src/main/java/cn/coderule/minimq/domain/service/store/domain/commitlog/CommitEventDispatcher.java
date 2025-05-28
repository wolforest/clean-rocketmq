package cn.coderule.minimq.domain.service.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.model.cluster.store.CommitEvent;

public interface CommitEventDispatcher {
    long getDispatchedOffset();
    void setDispatchedOffset(long offset);

    void registerHandler(CommitEventHandler handler);
    void dispatch(CommitEvent event);
}
