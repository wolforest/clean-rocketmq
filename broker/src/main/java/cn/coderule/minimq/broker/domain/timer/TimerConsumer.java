package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;

public class TimerConsumer extends ServiceThread {
    private final QueueTask task;

    public TimerConsumer(QueueTask task) {
        this.task = task;
    }

    @Override
    public String getServiceName() {
        return TimerConsumer.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
