package com.wolfmq.rpc.store.facade;

import com.wolfmq.common.server.RequestContext;
import com.wolfmq.common.domain.timer.model.ScanResult;
import com.wolfmq.common.domain.timer.model.TimerEvent;
import com.wolfmq.common.domain.timer.state.TimerCheckpoint;

public interface TimerFacade {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint(RequestContext context);

    boolean addTimer(TimerEvent event);
    ScanResult scan(RequestContext context, long delayTime);
}
