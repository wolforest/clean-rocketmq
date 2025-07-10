package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;

public class WriteService extends ServiceThread {
    @Override
    public String getServiceName() {
        return WriteService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
