package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;

public class TimerQueueConsumer extends ServiceThread {
    private final QueueTask task;

    public TimerQueueConsumer(QueueTask task) {
        this.task = task;
    }

    @Override
    public String getServiceName() {
        return TimerQueueConsumer.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
