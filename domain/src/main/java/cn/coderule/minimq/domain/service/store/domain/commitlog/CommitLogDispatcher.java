package cn.coderule.minimq.domain.service.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.model.cluster.store.CommitLogEvent;

public interface CommitLogDispatcher {
    long getDispatchedOffset();
    void setDispatchedOffset(long offset);

    void registerHandler(CommitLogHandler handler);
    void dispatch(CommitLogEvent event);
}
