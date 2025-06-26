package cn.coderule.minimq.domain.service.broker.infra.task;


public interface TaskLoader {
    void setTimerFactory(TaskFactory factory);
    void setReviveFactory(TaskFactory factory);
    void setTransactionFactory(TaskFactory factory);

    void load();
}
