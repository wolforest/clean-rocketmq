package com.wolfmq.domain.server.store.api;

import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;

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
