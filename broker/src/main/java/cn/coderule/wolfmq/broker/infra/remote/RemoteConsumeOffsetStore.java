package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.meta.offset.GroupResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.TopicResult;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.facade.ConsumeOffsetFacade;

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
