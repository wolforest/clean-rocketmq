package cn.coderule.minimq.domain.service.broker.infra.task;

import cn.coderule.minimq.domain.domain.cluster.task.StoreTaskSet;

public interface TaskFactory {
    void create(StoreTaskSet task);
}
