package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.MetaConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue.ConsumeQueueGateway;

public class NextOffsetSetter {
    private final StoreConfig storeConfig;
    private final ConsumeQueueGateway consumeQueue;

    public NextOffsetSetter(StoreConfig storeConfig, ConsumeQueueGateway consumeQueue) {
        this.storeConfig = storeConfig;
        this.consumeQueue = consumeQueue;
    }

    public void set(DequeueRequest request, DequeueResult result) {
        if (!consumeQueue.existsQueue(request.getTopic(), request.getQueueId())) {
            setOffsetIfQueueNotExists(request, result);
            return;
        }

        setMinOffset(request, result);
        setMaxOffset(request, result);

        if (0 == result.getMaxOffset()) {
            setOffsetIfQueueEmpty(request, result);
            return;
        }

        if (request.getOffset() < result.getMinOffset()) {
            setOffsetIfOffsetSmaller(request, result);
            return;
        }

        if (request.getOffset() == result.getMaxOffset()) {
            setOffsetIfOffsetEqualMax(request, result);
            return;
        }

        if (request.getOffset() > result.getMaxOffset()) {
            setOffsetIfOffsetBigger(request, result);
            return;
        }

        if (result.isEmpty()) {
            setOffsetIfResultEmpty(request, result);
            return;
        }

        setOffsetByMessageList(request, result);
    }

    private void setMinOffset(DequeueRequest request, DequeueResult result) {
        long offset = consumeQueue.getMinOffset(
            request.getGroup(),
            request.getQueueId()
        );

        result.setMinOffset(offset);
    }

    private void setMaxOffset(DequeueRequest request, DequeueResult result) {
        long offset = consumeQueue.getMaxOffset(
            request.getGroup(),
            request.getQueueId()
        );

        result.setMaxOffset(offset);
    }

    private long correctNextOffset(long oldOffset, long newOffset) {
        MetaConfig metaConfig = storeConfig.getMetaConfig();

        if (storeConfig.isMaster() || metaConfig.isEnableOffsetCheckInSlave()) {
            return newOffset;
        }

        return oldOffset;
    }

    private void setOffsetIfQueueNotExists(DequeueRequest request, DequeueResult result) {
        result.setStatus(MessageStatus.NO_MATCHED_LOGIC_QUEUE);
        result.setMinOffset(0);
        result.setMaxOffset(0);

        result.setNextOffset(
            correctNextOffset(request.getOffset(), 0)
        );
    }

    private void setOffsetIfQueueEmpty(DequeueRequest request, DequeueResult result) {
        result.setStatus(MessageStatus.NO_MESSAGE_IN_QUEUE);

        result.setNextOffset(
            correctNextOffset(request.getOffset(), 0)
        );
    }

    private void setOffsetIfResultEmpty(DequeueRequest request, DequeueResult result) {
        result.setStatus(MessageStatus.OFFSET_FOUND_NULL);

        long rolledOffset = consumeQueue.rollToOffset(
            request.getTopic(),
            request.getQueueId(),
            request.getOffset()
        );

        result.setNextOffset(
            correctNextOffset(request.getOffset(), rolledOffset)
        );
    }

    private void setOffsetIfOffsetSmaller(DequeueRequest request, DequeueResult result) {
        result.setStatus(MessageStatus.OFFSET_TOO_SMALL);

        result.setNextOffset(
            correctNextOffset(request.getOffset(), result.getMinOffset())
        );
    }

    private void setOffsetIfOffsetBigger(DequeueRequest request, DequeueResult result) {
        result.setStatus(MessageStatus.OFFSET_OVERFLOW_BADLY);

        result.setNextOffset(
            correctNextOffset(request.getOffset(), result.getMaxOffset())
        );
    }

    private void setOffsetIfOffsetEqualMax(DequeueRequest request, DequeueResult result) {
        result.setStatus(MessageStatus.OFFSET_OVERFLOW_ONE);

        result.setNextOffset(
            request.getOffset()
        );
    }

    private void setOffsetByMessageList(DequeueRequest request, DequeueResult result) {
        long maxOffset = 0;

        for (MessageBO messageBO : result.getMessageList()) {
            maxOffset = Math.max(maxOffset, messageBO.getQueueOffset());
        }

        result.setNextOffset(maxOffset + 1);
    }

}
