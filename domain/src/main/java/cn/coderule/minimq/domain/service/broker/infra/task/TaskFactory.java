package cn.coderule.minimq.domain.service.broker.infra.task;

import cn.coderule.minimq.domain.domain.cluster.store.StoreTask;

public interface TaskFactory {
    void create(StoreTask task);
}
