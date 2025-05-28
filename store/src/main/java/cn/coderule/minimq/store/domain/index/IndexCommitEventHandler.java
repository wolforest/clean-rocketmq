package cn.coderule.minimq.store.domain.index;

import cn.coderule.minimq.domain.domain.model.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventHandler;

public class IndexCommitEventHandler implements CommitEventHandler {
    @Override
    public void handle(CommitEvent event) {

    }
}
