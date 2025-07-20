package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.service.TimerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskScheduler extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final TimerQueue timerQueue;

    public TimerTaskScheduler(TimerContext context) {
        this.brokerConfig = context.getBrokerConfig();
        this.timerQueue = context.getTimerQueue();
    }

    @Override
    public String getServiceName() {
        return TimerTaskScheduler.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
