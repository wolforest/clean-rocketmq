package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.service.TimerContext;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerQueueConsumer extends ServiceThread {
    private final TimerContext context;
    private final TimerConfig timerConfig;
    private final QueueTask task;

    public TimerQueueConsumer(TimerContext context, QueueTask task) {
        this.context = context;
        this.timerConfig = context.getBrokerConfig().getTimerConfig();
        this.task = task;
    }

    @Override
    public String getServiceName() {
        return TimerQueueConsumer.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        long interval = 100L * timerConfig.getPrecision() / 1_000;

        while (!this.isStopped()) {
            try {
                if (!consume()) {
                    this.await(interval);
                }
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private boolean consume() {
        return false;
    }
}
