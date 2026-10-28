package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.domain.consumer.ack.AckInfo;
import com.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import com.wolfmq.domain.server.store.api.meta.AckStore;

@Deprecated
public class RemoteAckStore extends AbstractRemoteStore implements AckStore {
    public RemoteAckStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    @Override
    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {

    }

    @Override
    public void ack(AckInfo ackInfo, int reviveQueueId, long invisibleTime) {

    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        return -1;
    }
}
