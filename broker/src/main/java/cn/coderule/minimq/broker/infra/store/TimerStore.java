package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedTimerStore;
import cn.coderule.minimq.broker.infra.remote.RemoteTimerStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.rpc.store.facade.TimerFacade;

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

    }

    @Override
    public TimerCheckpoint loadCheckpoint(RequestContext context) {
        return null;
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        if (embedStore.isClusterGroup(event.getStoreGroup())) {
            return remoteStore.addTimer(event);
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
