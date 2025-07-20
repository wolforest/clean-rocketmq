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
    private final EmbedConsumeOffsetStore embedConsumeOffsetStore;
    private final RemoteConsumeOffsetStore remoteConsumeOffsetStore;

    public ConsumeOffsetStore(
        BrokerConfig brokerConfig,
        EmbedConsumeOffsetStore embedConsumeOffsetStore,
        RemoteConsumeOffsetStore remoteConsumeOffsetStore) {
        this.brokerConfig = brokerConfig;
        this.embedConsumeOffsetStore = embedConsumeOffsetStore;
        this.remoteConsumeOffsetStore = remoteConsumeOffsetStore;
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        String topicName = request.getTopicName();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            return embedConsumeOffsetStore.getOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return OffsetResult.build(0);
        }
        return remoteConsumeOffsetStore.getOffset(request);
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        String topicName = request.getTopicName();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            return embedConsumeOffsetStore.getAndRemove(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return OffsetResult.build(0);
        }

        return remoteConsumeOffsetStore.getAndRemove(request);
    }

    @Override
    public void putOffset(OffsetRequest request) {
        String topicName = request.getTopicName();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            embedConsumeOffsetStore.putOffset(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteConsumeOffsetStore.putOffset(request);
    }

    @Override
    public void deleteByTopic(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            embedConsumeOffsetStore.deleteByTopic(filter);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteConsumeOffsetStore.deleteByTopic(filter);
    }

    @Override
    public void deleteByGroup(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            embedConsumeOffsetStore.deleteByGroup(filter);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteConsumeOffsetStore.deleteByGroup(filter);
    }

    @Override
    public TopicResult findTopicByGroup(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            return embedConsumeOffsetStore.findTopicByGroup(filter);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return TopicResult.empty();
        }

        return remoteConsumeOffsetStore.findTopicByGroup(filter);
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        String topicName = filter.getTopic();
        if (embedConsumeOffsetStore.isEmbed(topicName)) {
            return embedConsumeOffsetStore.findGroupByTopic(filter);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return GroupResult.empty();
        }

        return remoteConsumeOffsetStore.findGroupByTopic(filter);
    }
}
