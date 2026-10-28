package com.wolfmq.domain.server.task;

public interface TaskFactory {
    void create(QueueTask task);
    void destroy(QueueTask task);
}
