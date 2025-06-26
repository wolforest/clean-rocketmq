package cn.coderule.minimq.domain.service.broker.infra.task;


public interface TaskLoader {
    void addTimerFactory(TaskFactory factory);
    void addReviveFactory(TaskFactory factory);
    void addTransactionFactory(TaskFactory factory);
}
