package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.service.store.domain.timer.Timer;

public class BlackHoleTimer implements Timer {
    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {

    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return new TimerCheckpoint();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan() {
        return new ScanResult();
    }
}
