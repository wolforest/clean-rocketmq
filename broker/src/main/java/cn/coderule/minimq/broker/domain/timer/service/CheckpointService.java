package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckpointService extends ServiceThread {
    private final TimerContext context;

    private QueueTask queueTask;

    public CheckpointService(TimerContext context) {
        this.context = context;
    }

    @Override
    public String getServiceName() {
        return CheckpointService.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    private boolean loadQueueTask() {
        log.debug("load queue task");

        try {
            queueTask = context.getOrWaitQueueTask();
        } catch (Exception e) {
            log.error("load queue task error", e);
            return false;
        }

        return true;
    }
}
