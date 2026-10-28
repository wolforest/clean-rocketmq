package com.wolfmq.domain.domain.commitlog;

public interface CommitEventDispatcher {
    long getDispatchedOffset();
    void setDispatchedOffset(long offset);

    void registerHandler(CommitHandler handler);
    void dispatch(CommitEvent event);
}
