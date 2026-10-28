package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.client.TimerClient;
import com.wolfmq.rpc.store.facade.TimerFacade;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteTimerStore extends AbstractRemoteStore implements TimerFacade {
    private final BrokerConfig brokerConfig;
    private final ConcurrentMap<String, TimerClient> clientMap;
    private final RpcClient rpcClient;

    public RemoteTimerStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        clientMap = new ConcurrentHashMap<>();
        this.rpcClient = rpcClient;
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
