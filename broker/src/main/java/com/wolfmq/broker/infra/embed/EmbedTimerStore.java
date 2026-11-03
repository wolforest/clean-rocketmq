package com.wolfmq.broker.infra.embed;

import com.wolfmq.common.server.broker.RequestContext;
import com.wolfmq.common.domain.timer.model.ScanResult;
import com.wolfmq.common.domain.timer.model.TimerEvent;
import com.wolfmq.common.domain.timer.state.TimerCheckpoint;
import com.wolfmq.common.server.store.api.TimerStore;
import com.wolfmq.rpc.store.facade.TimerFacade;

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
