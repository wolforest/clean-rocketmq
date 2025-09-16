package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.store.domain.timer.Timer;

public class BlackHoleTimer implements Timer {
    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan(long delayTime) {
        return new ScanResult();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
