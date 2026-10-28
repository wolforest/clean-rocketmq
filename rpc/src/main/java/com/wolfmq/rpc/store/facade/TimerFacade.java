package com.wolfmq.rpc.store.facade;

import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;

public interface TimerFacade {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint(RequestContext context);

    boolean addTimer(TimerEvent event);
    ScanResult scan(RequestContext context, long delayTime);
}
