package cn.coderule.minimq.domain.domain.store.domain.commitlog;

public interface CommitEventDispatcher {
    long getDispatchedOffset();
    void setDispatchedOffset(long offset);

    void registerHandler(CommitEventHandler handler);
    void dispatch(CommitEvent event);
}
