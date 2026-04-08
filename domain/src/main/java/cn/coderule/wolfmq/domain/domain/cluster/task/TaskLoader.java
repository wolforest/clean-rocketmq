package cn.coderule.wolfmq.domain.domain.cluster.task;


public interface TaskLoader {
    void registerTimerFactory(TaskFactory factory);
    void registerReviveFactory(TaskFactory factory);
    void registerTransactionFactory(TaskFactory factory);

    void load();
}
