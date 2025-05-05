package cn.coderule.minimq.domain.service.store.domain;

import cn.coderule.minimq.domain.domain.model.store.CommitLogEvent;

public interface CommitLogDispatcher {
    long getDispatchedOffset();
    void setDispatchedOffset(long offset);

    void registerHandler(CommitLogHandler handler);
    void dispatch(CommitLogEvent event);
}
