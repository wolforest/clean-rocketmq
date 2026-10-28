package com.wolfmq.domain.domain.commitlog;

/**
 * @renamed from CommitEventHandler to CommitHandler
 */
public interface CommitHandler {
    void handle(CommitEvent event);
}
