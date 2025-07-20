package cn.coderule.minimq.rpc.store.facade;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;

public interface TimerFacade {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint();

    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);
}
