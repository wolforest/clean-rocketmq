package cn.coderule.wolfmq.domain.domain.cluster.task;

public interface TaskFactory {
    void create(QueueTask task);
    void destroy(QueueTask task);
}
