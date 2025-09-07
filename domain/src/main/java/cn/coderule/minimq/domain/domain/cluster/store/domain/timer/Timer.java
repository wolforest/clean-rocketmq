package cn.coderule.minimq.domain.domain.cluster.store.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;

public interface Timer extends Lifecycle {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint();

    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

}
