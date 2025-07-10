package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;

public class ReadService extends ServiceThread {
    @Override
    public String getServiceName() {
        return ReadService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
