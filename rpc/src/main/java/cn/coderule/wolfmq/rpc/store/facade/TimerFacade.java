package cn.coderule.wolfmq.rpc.store.facade;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;

public interface TimerFacade {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint(RequestContext context);

    boolean addTimer(TimerEvent event);
    ScanResult scan(RequestContext context, long delayTime);
}
