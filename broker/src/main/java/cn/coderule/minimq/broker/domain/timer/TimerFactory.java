package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.service.broker.infra.task.TaskFactory;

public class TimerFactory implements TaskFactory, Lifecycle {
    @Override
    public void create(QueueTask task) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
