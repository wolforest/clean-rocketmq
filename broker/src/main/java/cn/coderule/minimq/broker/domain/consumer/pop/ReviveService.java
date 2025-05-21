package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;

public class ReviveService extends ServiceThread {
    @Override
    public String getServiceName() {
        return ReviveService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
