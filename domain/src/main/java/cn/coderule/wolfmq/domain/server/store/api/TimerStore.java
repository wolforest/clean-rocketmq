package cn.coderule.wolfmq.domain.server.store.api;

import cn.coderule.wolfmq.domain.domain.timer.model.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.model.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;

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
