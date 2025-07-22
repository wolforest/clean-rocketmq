package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.infra.store.TimerStore;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskScheduler extends ServiceThread {
    private final TimerContext timerContext;
    private final TimerConfig timerConfig;
    private final TimerQueue timerQueue;
    private final TimerState timerState;
    private final TimerStore timerStore;

    private QueueTask queueTask;

    public TimerTaskScheduler(TimerContext context) {
        this.timerContext = context;
        BrokerConfig brokerConfig = context.getBrokerConfig();
        this.timerConfig = brokerConfig.getTimerConfig();
        this.timerQueue = context.getTimerQueue();
        this.timerState = context.getTimerState();
        this.timerStore = context.getTimerStore();
    }

    @Override
    public String getServiceName() {
        return TimerTaskScheduler.class.getSimpleName();
    }

    @Override
    public void run() {
        setState(State.STARTING);
        log.info("{} service started", this.getServiceName());
        if (!loadQueueTask()) {
            return;
        }

        while (!this.isStopped()) {
            try {
                schedule();
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
        setState(State.ENDING);
    }

    private void schedule() throws InterruptedException {
        setState(State.WAITING);

        long timeout = 100L * timerConfig.getPrecision() / 1_000;
        List<TimerEvent> eventList = timerQueue.pollScheduleEvent(timeout);;
        if (CollectionUtil.isEmpty(eventList)) {
            return;
        }

        setState(State.RUNNING);
        process(eventList);
        eventList.clear();
    }

    private void process(List<TimerEvent> eventList) {
        for (TimerEvent event : eventList) {
            process(event);
        }
    }

    private void process(TimerEvent event) {

    }

    private boolean loadQueueTask() {
        log.debug("load queue task");

        try {
            queueTask = timerContext.getOrWaitQueueTask();
        } catch (Exception e) {
            log.error("load queue task error", e);
            return false;
        }

        return true;
    }
}
