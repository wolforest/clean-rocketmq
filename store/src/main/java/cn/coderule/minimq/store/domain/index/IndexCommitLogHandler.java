package cn.coderule.minimq.store.domain.index;

import cn.coderule.minimq.domain.domain.model.cluster.store.CommitLogEvent;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogHandler;

public class IndexCommitLogHandler implements CommitLogHandler {
    @Override
    public void handle(CommitLogEvent event) {

    }
}
