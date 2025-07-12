package cn.coderule.minimq.store.server;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;

public class WarmupService extends ServiceThread implements Lifecycle {
    @Override
    public String getServiceName() {
        return WarmupService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
