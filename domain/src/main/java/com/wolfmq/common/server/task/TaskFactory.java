package com.wolfmq.common.server.task;

public interface TaskFactory {
    void create(QueueTask task);
    void destroy(QueueTask task);
}
