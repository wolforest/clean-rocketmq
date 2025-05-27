package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.model.consumer.pop.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import cn.coderule.minimq.domain.service.store.api.AckStore;

public class RemoteAckStore extends AbstractRemoteStore implements AckStore {
    public RemoteAckStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    @Override
    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {

    }

    @Override
    public void ack(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {

    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        return -1;
    }
}
