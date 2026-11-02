package com.wolfmq.common.server.task;


public interface TaskLoader {
    void registerTimerFactory(TaskFactory factory);
    void registerReviveFactory(TaskFactory factory);
    void registerTransactionFactory(TaskFactory factory);

    void load();
}
