package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.cluster.store.api.TimerStore;
import cn.coderule.minimq.store.domain.timer.service.TimerService;

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
