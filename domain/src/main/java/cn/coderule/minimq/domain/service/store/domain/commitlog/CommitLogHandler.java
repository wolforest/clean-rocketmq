package cn.coderule.minimq.domain.service.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.model.cluster.store.CommitLogEvent;

public interface CommitLogHandler {
    void handle(CommitLogEvent event);
}
