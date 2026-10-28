package com.wolfmq.store.api;

import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.store.domain.timer.service.TimerService;

public class TimerStoreImpl implements TimerStore {
    private final TimerService timerService;

    public TimerStoreImpl(TimerService timerService) {
        this.timerService = timerService;
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {
        timerService.storeCheckpoint(checkpoint);
    }

    @Override
    public TimerCheckpoint getCheckpoint() {
        return timerService.loadCheckpoint();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return timerService.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return timerService.scan(delayTime);
    }

    @Override
    public String getMetricJson() {
        return "";
    }
}
