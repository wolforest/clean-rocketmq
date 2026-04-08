package cn.coderule.wolfmq.rpc.store.client.meta;

import cn.coderule.wolfmq.domain.domain.meta.offset.GroupResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.TopicResult;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.StoreClient;
import cn.coderule.wolfmq.rpc.store.client.AbstractStoreClient;
import cn.coderule.wolfmq.rpc.store.facade.ConsumeOffsetFacade;

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
