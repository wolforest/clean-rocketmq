package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.offset.GroupResult;
import com.wolfmq.domain.domain.meta.offset.OffsetFilter;
import com.wolfmq.domain.domain.meta.offset.OffsetRequest;
import com.wolfmq.domain.domain.meta.offset.OffsetResult;
import com.wolfmq.domain.domain.meta.offset.TopicResult;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;

public class RemoteConsumeOffsetStore extends AbstractRemoteStore implements ConsumeOffsetFacade {
    private final BrokerConfig brokerConfig;
    private final RpcClient rpcClient;

    public RemoteConsumeOffsetStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        this.rpcClient = rpcClient;
    }
    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        return OffsetResult.notFound();
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        return null;
    }

    @Override
    public void putOffset(OffsetRequest request) {

    }

    @Override
    public void deleteByTopic(OffsetFilter filter) {

    }

    @Override
    public void deleteByGroup(OffsetFilter filter) {

    }

    @Override
    public TopicResult findTopicByGroup(OffsetFilter filter) {
        return TopicResult.empty();
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        return GroupResult.empty();
    }
}
