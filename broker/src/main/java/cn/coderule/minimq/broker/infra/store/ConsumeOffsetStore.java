package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedConsumeOffsetStore;
import cn.coderule.minimq.broker.infra.remote.RemoteConsumeOffsetStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.offset.GroupResult;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.domain.meta.offset.TopicResult;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;

public class ConsumeOffsetStore implements ConsumeOffsetFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedConsumeOffsetStore embedStore;
    private final RemoteConsumeOffsetStore remoteStore;

    public ConsumeOffsetStore(
        BrokerConfig brokerConfig,
        EmbedConsumeOffsetStore embedStore,
        RemoteConsumeOffsetStore remoteStore
    ) {
        this.brokerConfig = brokerConfig;
        this.embedStore = embedStore;
        this.remoteStore = remoteStore;
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        String topicName = request.getTopicName();
        if (embedStore.containsTopic(topicName)) {
            return embedStore.getOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return OffsetResult.build(0);
        }
        return remoteStore.getOffset(request);
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        String topicName = request.getTopicName();
        if (embedStore.containsTopic(topicName)) {
            return embedStore.getAndRemove(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return OffsetResult.build(0);
        }

        return remoteStore.getAndRemove(request);
    }

    @Override
    public void putOffset(OffsetRequest request) {
        String topicName = request.getTopicName();
        if (embedStore.containsTopic(topicName)) {
            embedStore.putOffset(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.putOffset(request);
    }

    @Override
    public void deleteByTopic(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedStore.containsTopic(topicName)) {
            embedStore.deleteByTopic(filter);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.deleteByTopic(filter);
    }

    @Override
    public void deleteByGroup(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedStore.containsTopic(topicName)) {
            embedStore.deleteByGroup(filter);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteStore.deleteByGroup(filter);
    }

    @Override
    public TopicResult findTopicByGroup(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedStore.containsTopic(topicName)) {
            return embedStore.findTopicByGroup(filter);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return TopicResult.empty();
        }

        return remoteStore.findTopicByGroup(filter);
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedStore.containsTopic(topicName)) {
            return embedStore.findGroupByTopic(filter);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return GroupResult.empty();
        }

        return remoteStore.findGroupByTopic(filter);
    }
}
