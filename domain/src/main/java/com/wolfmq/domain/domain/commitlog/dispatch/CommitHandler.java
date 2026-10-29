package com.wolfmq.domain.domain.commitlog.dispatch;

/**
 * @renamed from CommitEventHandler to CommitHandler
 */
public interface CommitHandler {
    void handle(CommitEvent event);
}
