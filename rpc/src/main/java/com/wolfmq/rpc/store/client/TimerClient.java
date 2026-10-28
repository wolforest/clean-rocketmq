package com.wolfmq.rpc.store.client;

import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.StoreClient;
import com.wolfmq.rpc.store.facade.TimerFacade;

public class TimerClient extends AbstractStoreClient implements StoreClient, TimerFacade {
    public TimerClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {

    }

    @Override
    public TimerCheckpoint loadCheckpoint(RequestContext context) {
        return null;
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan(RequestContext context, long delayTime) {
        return null;
    }
}
