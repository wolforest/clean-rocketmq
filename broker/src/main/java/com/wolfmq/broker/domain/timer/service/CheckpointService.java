package com.wolfmq.broker.domain.timer.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import com.wolfmq.broker.domain.timer.context.TimerContext;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.server.task.QueueTask;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.domain.domain.timer.state.TimerState;
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
        if (!loadQueueTask()) {
            log.error("load queue task error");
            return;
        }

        String storeGroup = queueTask.getStoreGroup();
        RequestContext requestContext = RequestContext.create(storeGroup);
        TimerCheckpoint checkpoint = context.getTimerStore().loadCheckpoint(requestContext);

        TimerState timerState = context.getTimerState();
        timerState.update(checkpoint);
    }

    private void store() {
        TimerCheckpoint checkpoint = context.getTimerState().toCheckpoint();
        context.getTimerStore().storeCheckpoint(checkpoint);
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
