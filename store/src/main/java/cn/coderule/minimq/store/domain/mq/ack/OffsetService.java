package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;

public class OffsetService {
    private DequeueLock dequeueLock;
    private ConsumeOffsetService consumeOffsetService;

    public void ack(AckMessage ackMessage) {
        if (!ackMessage.isConsumeOrderly()) {
            return;
        }

        if (isOffsetOld(ackMessage)) {
            return;
        }

        lock(ackMessage);

        try {
            updateOffset(ackMessage);
        } finally {
            unlock(ackMessage);
        }
    }

    private boolean isOffsetOld(AckMessage ackMessage) {
        return false;
    }

    private void updateOffset(AckMessage ackMessage) {

    }

    private void lock(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        dequeueLock.lock(
            ackInfo.getTopic(),
            ackInfo.getConsumerGroup(),
            ackInfo.getQueueId()
        );
    }

    private void unlock(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        dequeueLock.unlock(
            ackInfo.getTopic(),
            ackInfo.getConsumerGroup(),
            ackInfo.getQueueId()
        );
    }
}
