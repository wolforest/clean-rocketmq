package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.meta.offset.GroupResult;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.domain.meta.offset.TopicResult;
import cn.coderule.minimq.domain.service.broker.infra.meta.ConsumeOffsetStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;

public class ConsumeOffsetClient extends AbstractStoreClient implements StoreClient, ConsumeOffsetStore {
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
