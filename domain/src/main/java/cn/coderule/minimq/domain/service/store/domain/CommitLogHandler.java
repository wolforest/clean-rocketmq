package cn.coderule.minimq.domain.service.store.domain;

import cn.coderule.minimq.domain.domain.model.CommitLogEvent;

public interface CommitLogHandler {
    void handle(CommitLogEvent event);
}
