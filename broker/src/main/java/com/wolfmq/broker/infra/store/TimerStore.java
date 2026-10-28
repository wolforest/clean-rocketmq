package com.wolfmq.broker.infra.store;

import com.wolfmq.broker.infra.embed.EmbedTimerStore;
import com.wolfmq.broker.infra.remote.RemoteTimerStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.rpc.store.facade.TimerFacade;

public class TimerStore implements TimerFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedTimerStore embedStore;
    private final RemoteTimerStore remoteStore;

    public TimerStore(BrokerConfig brokerConfig, EmbedTimerStore embedStore, RemoteTimerStore remoteStore) {
        this.brokerConfig = brokerConfig;
        this.embedStore = embedStore;
        this.remoteStore = remoteStore;
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {
        if (embedStore.isClusterGroup(checkpoint.getStoreGroup())) {
            embedStore.storeCheckpoint(checkpoint);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.storeCheckpoint(checkpoint);
    }

    @Override
    public TimerCheckpoint loadCheckpoint(RequestContext context) {
        if (embedStore.isClusterGroup(context.getStoreGroup())) {
            return embedStore.loadCheckpoint(context);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteStore.loadCheckpoint(context);
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        if (embedStore.isClusterGroup(event.getStoreGroup())) {
            return embedStore.addTimer(event);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return false;
        }

        return remoteStore.addTimer(event);
    }

    @Override
    public ScanResult scan(RequestContext context, long delayTime) {
        if (embedStore.isClusterGroup(context.getStoreGroup())) {
            return embedStore.scan(context, delayTime);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return ScanResult.empty();
        }

        return remoteStore.scan(context, delayTime);
    }
}
