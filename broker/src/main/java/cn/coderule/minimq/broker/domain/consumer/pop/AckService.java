package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;

public class AckService extends ServiceThread {
    @Override
    public String getServiceName() {
        return AckService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
