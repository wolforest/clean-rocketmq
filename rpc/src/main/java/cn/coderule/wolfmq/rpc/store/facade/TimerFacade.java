package cn.coderule.wolfmq.rpc.store.facade;

import cn.coderule.wolfmq.domain.server.RequestContext;
import cn.coderule.wolfmq.domain.domain.timer.model.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.model.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;

public interface TimerFacade {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint(RequestContext context);

    boolean addTimer(TimerEvent event);
    ScanResult scan(RequestContext context, long delayTime);
}
