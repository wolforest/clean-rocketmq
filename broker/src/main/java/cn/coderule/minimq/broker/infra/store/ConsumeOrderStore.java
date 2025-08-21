package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedConsumeOrderStore;
import cn.coderule.minimq.broker.infra.remote.RemoteConsumeOrderStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.rpc.store.facade.ConsumeOrderFacade;

public class ConsumeOrderStore implements ConsumeOrderFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedConsumeOrderStore embedStore;
    private final RemoteConsumeOrderStore remoteStore;

    public ConsumeOrderStore(
        BrokerConfig brokerConfig,
        EmbedConsumeOrderStore embedStore,
        RemoteConsumeOrderStore remoteStore
    ) {
        this.brokerConfig = brokerConfig;

        this.embedStore = embedStore;
        this.remoteStore = remoteStore;
    }

    @Override
    public boolean isLocked(OrderRequest request) {
        if (embedStore.containsTopic(request.getTopicName())) {
            return embedStore.isLocked(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return false;
        }
        return remoteStore.isLocked(request);
    }

    @Override
    public void lock(OrderRequest request) {
        if (embedStore.containsTopic(request.getTopicName())) {
            embedStore.lock(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.lock(request);
    }

    @Override
    public void unlock(OrderRequest request) {
        if (embedStore.containsTopic(request.getTopicName())) {
            embedStore.unlock(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.unlock(request);
    }

    @Override
    public long commit(OrderRequest request) {
        if (embedStore.containsTopic(request.getTopicName())) {
            return embedStore.commit(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return -1;
        }

        return remoteStore.commit(request);
    }

    @Override
    public void updateInvisible(OrderRequest request) {
        if (embedStore.containsTopic(request.getTopicName())) {
            embedStore.updateInvisible(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.updateInvisible(request);
    }
}
