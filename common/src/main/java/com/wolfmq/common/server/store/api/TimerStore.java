package com.wolfmq.common.server.store.api;

import com.wolfmq.common.domain.timer.model.ScanResult;
import com.wolfmq.common.domain.timer.model.TimerEvent;
import com.wolfmq.common.domain.timer.state.TimerCheckpoint;

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
