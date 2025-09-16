package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WarmupService extends ServiceThread {
    @Override
    public String getServiceName() {
        return WarmupService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
