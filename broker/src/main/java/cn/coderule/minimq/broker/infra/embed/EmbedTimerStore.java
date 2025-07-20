package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.service.store.api.TimerStore;
import cn.coderule.minimq.rpc.store.facade.TimerFacade;

public class EmbedTimerStore extends AbstractEmbedStore implements TimerFacade {
    private final TimerStore timerStore;

    public EmbedTimerStore(TimerStore timerStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.timerStore = timerStore;
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {
        timerStore.storeCheckpoint(checkpoint);
    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return timerStore.loadCheckpoint();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return timerStore.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return timerStore.scan(delayTime);
    }
}
