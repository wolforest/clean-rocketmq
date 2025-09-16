package cn.coderule.minimq.domain.domain.store.api;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;

/**
 * Timer APIs, for M/S
 */
public interface TimerStore {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint getCheckpoint();

    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

    String getMetricJson();
}
