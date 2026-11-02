package com.wolfmq.common.domain.commitlog.dispatch;

/**
 * @renamed from CommitEventHandler to CommitHandler
 */
public interface CommitHandler {
    void handle(CommitEvent event);
}
