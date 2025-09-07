package cn.coderule.minimq.domain.domain.cluster.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;

public interface CommitEventHandler {
    void handle(CommitEvent event);
}
