package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedTimerStore;
import cn.coderule.minimq.broker.infra.remote.RemoteTimerStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.rpc.store.facade.TimerFacade;

public class TimerStore implements TimerFacade {
    private final BrokerConfig brokerConfig;
    private final TimerFacade embedTimerStore;
    private final TimerFacade remoteTimerStore;

    public TimerStore(BrokerConfig brokerConfig, EmbedTimerStore embedTimerStore, RemoteTimerStore remoteTimerStore) {
        this.brokerConfig = brokerConfig;
        this.embedTimerStore = embedTimerStore;
        this.remoteTimerStore = remoteTimerStore;
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {

    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return null;
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan(long delayTime) {
        return null;
    }
}
