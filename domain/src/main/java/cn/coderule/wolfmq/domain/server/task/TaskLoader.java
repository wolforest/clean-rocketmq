package cn.coderule.wolfmq.domain.server.task;


public interface TaskLoader {
    void registerTimerFactory(TaskFactory factory);
    void registerReviveFactory(TaskFactory factory);
    void registerTransactionFactory(TaskFactory factory);

    void load();
}
