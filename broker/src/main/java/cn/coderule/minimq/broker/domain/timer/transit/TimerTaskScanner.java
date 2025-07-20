package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.service.TimerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskScanner extends ServiceThread {
    private final TimerContext timerContext;
    private final BrokerConfig brokerConfig;
    private final TimerQueue timerQueue;

    private QueueTask queueTask;

    public TimerTaskScanner(TimerContext context) {
        this.timerContext = context;
        this.brokerConfig = context.getBrokerConfig();
        this.timerQueue = context.getTimerQueue();
    }

    @Override
    public String getServiceName() {
        return TimerTaskScanner.class.getSimpleName();
    }

    @Override
    public void run() {

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

    private void scan() {

    }
}
