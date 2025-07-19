package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskStore extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final TimerQueue timerQueue;

    public TimerTaskStore(BrokerConfig brokerConfig, TimerQueue timerQueue) {
        this.brokerConfig = brokerConfig;
        this.timerQueue = timerQueue;
    }

    @Override
    public String getServiceName() {
        return TimerTaskStore.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
