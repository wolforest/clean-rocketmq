package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;

public class TimerConsumer extends ServiceThread {
    @Override
    public String getServiceName() {
        return TimerConsumer.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
