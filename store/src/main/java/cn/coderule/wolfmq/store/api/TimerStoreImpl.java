package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.wolfmq.domain.domain.store.api.TimerStore;
import cn.coderule.wolfmq.store.domain.timer.service.TimerService;

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
