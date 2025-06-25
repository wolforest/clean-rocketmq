package cn.coderule.minimq.domain.service.broker.infra;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.cluster.store.StoreTask;

public interface TaskFactory {
    ServiceThread create(StoreTask task);
}
