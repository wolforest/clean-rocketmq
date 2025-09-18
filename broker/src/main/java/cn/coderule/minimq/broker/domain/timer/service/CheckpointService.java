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
    public void initialize() {
        log.info("{} service initializing", this.getServiceName());

        if (!loadQueueTask()) {
            return;
        }

        load();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());
        if (!loadQueueTask()) {
            return;
        }

        while (!this.isStopped()) {
            try {
                store();
                await(getInterval());
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private void load() {

    }

    private void store() {

    }

    private int getInterval() {
        return context
            .getBrokerConfig()
            .getTimerConfig()
            .getFlushInterval();
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
