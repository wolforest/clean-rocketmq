package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.server.RequestContext;
import cn.coderule.wolfmq.domain.domain.timer.model.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.model.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.wolfmq.domain.server.store.api.TimerStore;
import cn.coderule.wolfmq.rpc.store.facade.TimerFacade;

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
