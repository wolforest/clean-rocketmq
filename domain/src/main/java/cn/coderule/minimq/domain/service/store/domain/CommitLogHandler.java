package cn.coderule.minimq.domain.service.store.domain;

import cn.coderule.minimq.domain.model.bo.CommitLogEvent;

public interface CommitLogHandler {
    void handle(CommitLogEvent event);
}
