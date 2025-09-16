package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.store.api.TimerStore;
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
    public TimerCheckpoint loadCheckpoint(RequestContext context) {
        return timerStore.getCheckpoint();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return timerStore.addTimer(event);
    }

    @Override
    public ScanResult scan(RequestContext context, long delayTime) {
        return timerStore.scan(delayTime);
    }
}
