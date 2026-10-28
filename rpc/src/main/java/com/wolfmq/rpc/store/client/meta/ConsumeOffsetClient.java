package com.wolfmq.rpc.store.client.meta;

import com.wolfmq.domain.domain.meta.offset.GroupResult;
import com.wolfmq.domain.domain.meta.offset.OffsetFilter;
import com.wolfmq.domain.domain.meta.offset.OffsetRequest;
import com.wolfmq.domain.domain.meta.offset.OffsetResult;
import com.wolfmq.domain.domain.meta.offset.TopicResult;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.StoreClient;
import com.wolfmq.rpc.store.client.AbstractStoreClient;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;

public class ConsumeOffsetClient extends AbstractStoreClient implements StoreClient, ConsumeOffsetFacade {
    public ConsumeOffsetClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        return null;
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
        return null;
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        return null;
    }
}
