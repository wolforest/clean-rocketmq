package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.AckBuffer;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopKeyBuilder;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {
    private final StoreConfig storeConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;

    private final AckBuffer ackBuffer;
    private final OffsetService offsetService;
    private final EnqueueService enqueueService;

    public AckService(
        StoreConfig storeConfig,
        String reviveTopic,
        AckBuffer ackBuffer,
        EnqueueService enqueueService,
        OffsetService offsetService
    ) {
        this.storeConfig  = storeConfig;
        this.messageConfig  = storeConfig.getMessageConfig();
        this.reviveTopic = reviveTopic;

        this.ackBuffer = ackBuffer;
        this.enqueueService = enqueueService;
        this.offsetService = offsetService;
    }

    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveOffset, long nextOffset) {
        PopCheckPointWrapper pointWrapper = new PopCheckPointWrapper(
            reviveQueueId,
            reviveOffset,
            point,
            nextOffset
        );

        if (ackBuffer.containsKey(pointWrapper.getMergeKey())) {
            log.warn("Duplicate checkpoint, key:{}, checkpoint: {}", pointWrapper.getMergeKey(), pointWrapper);
            return;
        }

        if (!messageConfig.isEnablePopBufferMerge()) {
            enqueueReviveQueue(pointWrapper);
            return;
        }

        ackBuffer.enqueue(pointWrapper);
    }

    public void ack(AckMessage ackMessage) {
        offsetService.ack(ackMessage);

        if (!messageConfig.isEnablePopBufferMerge()) {
            MessageBO messageBO = buildAckMsg(ackMessage);
            enqueueReviveQueue(messageBO);
            return;
        }

        try {
            mergeAckMsg(ackMessage.getAckInfo(), ackMessage.getReviveQueueId());
        } catch (Throwable t) {
            log.error("[PopBuffer]ack error, reviveQueueId: {}. ",
                ackMessage.getReviveQueueId(), t);
        }
    }

    public void nack(AckInfo ackInfo, int reviveQueueId, long invisibleTime) {
        if (!messageConfig.isEnablePopBufferMerge()) {
            MessageBO messageBO = buildNackMsg(ackInfo, reviveQueueId, invisibleTime);
            enqueueReviveQueue(messageBO);
            return;
        }

        try {
            mergeAckMsg(ackInfo, reviveQueueId);
        } catch (Throwable t) {
            log.error("[PopBuffer]ack error, reviveQueueId: {}. ",
                reviveQueueId, t);
        }
    }

    public long getBufferedOffset(String group, String topic, int queueId) {
        String lockKey = KeyBuilder.buildConsumeKey(topic, group, queueId);
        return ackBuffer.getLatestOffset(lockKey);
    }

    public int getTotalSize() {
        return ackBuffer.getTotalSize();
    }

    public int getBufferedSize() {
        return ackBuffer.getCount();
    }

    private PopCheckPointWrapper getCheckPoint(AckInfo ackInfo) {
        String key = PopKeyBuilder.buildKey(ackInfo);
        PopCheckPointWrapper pointWrapper = ackBuffer.getCheckPoint(key);
        if (pointWrapper != null) {
            return pointWrapper;
        }

        if (messageConfig.isEnablePopLog()) {
            log.info("[PopBuffer]can't find ackMsg related PopCheckPointWrapper: {}", ackInfo);
        }

        return null;
    }

    private boolean validateCheckPoint(PopCheckPointWrapper pointWrapper, AckInfo ackInfo, int reviveQueueId) {
        if (pointWrapper == null) {
            return false;
        }

        if (pointWrapper.isJustOffset()) {
            return false;
        }


        PopCheckPoint point = pointWrapper.getCk();
        long now = System.currentTimeMillis();

        if (point.getReviveTime() - now < messageConfig.getPopCkStayBufferTimeOut() + 1500) {
            if (messageConfig.isEnablePopLog()) {
                log.warn("[PopBuffer]add ack fail, rqId={}, almost timeout for revive, {}, {}, {}",
                    reviveQueueId, pointWrapper, ackInfo, now);
            }
            return false;
        }

        if (now - point.getPopTime() > messageConfig.getPopCkStayBufferTime() - 1500) {
            if (messageConfig.isEnablePopLog()) {
                log.warn("[PopBuffer]add ack fail, rqId={}, stay too long, {}, {}, {}",
                    reviveQueueId, pointWrapper, ackInfo, now);
            }
            return false;
        }

        return true;
    }

    private void mergeAckMsg(AckInfo ackInfo, int reviveQueueId) {
        PopCheckPointWrapper pointWrapper = getCheckPoint(ackInfo);
        if (pointWrapper == null) {
            return;
        }

        if (!validateCheckPoint(pointWrapper, ackInfo, reviveQueueId)) {
            return;
        }

        if (ackInfo instanceof BatchAckInfo) {
            mergeBatchAckMsg((BatchAckInfo) ackInfo, pointWrapper);
            return;
        }

        mergeAckMsg(ackInfo, pointWrapper);
    }

    private void mergeAckMsg(AckInfo ackInfo, PopCheckPointWrapper pointWrapper) {
        mergeByOffset(ackInfo, pointWrapper, ackInfo.getAckOffset());
    }

    private void mergeBatchAckMsg(BatchAckInfo ackMsg, PopCheckPointWrapper pointWrapper) {
        for (Long offset : ackMsg.getAckOffsetList()) {
            mergeByOffset(ackMsg, pointWrapper, offset);
        }
    }

    private void mergeByOffset(AckInfo ackInfo, PopCheckPointWrapper pointWrapper, long offset) {
        int index = pointWrapper.getCk().indexOfAck(offset);
        if (index > -1) {
            markBitCAS(pointWrapper.getBits(), index);
            return;
        }

        log.error("[PopBuffer]invalid index of BatchAckMsg, ackMsg: {}, index: {}, checkPoint: {}",
            ackInfo, index, pointWrapper);
    }

    private void markBitCAS(AtomicInteger setBits, int index) {
        while (true) {
            int bits = setBits.get();
            if (ByteUtil.getBit(bits, index)) {
                break;
            }

            int newBits = ByteUtil.setBit(bits, index, true);
            if (setBits.compareAndSet(bits, newBits)) {
                break;
            }
        }
    }

    private MessageBO buildAckMsg(AckMessage ackMessage) {
        return AckConverter.toMessage(
            ackMessage.getAckInfo(),
            reviveTopic,
            ackMessage.getReviveQueueId(),
            ackMessage.getInvisibleTime(),
            storeConfig.getHostAddress()
        );
    }
    private MessageBO buildNackMsg(AckInfo ackInfo, int reviveQueueId, long invisibleTime) {
        return AckConverter.toMessage(
            ackInfo, reviveTopic, reviveQueueId, invisibleTime, storeConfig.getHostAddress()
        );
    }

    private void enqueueReviveQueue(MessageBO messageBO) {
        EnqueueResult result = enqueueService.enqueue(messageBO);
        if (result.isFailure()) {
            log.error("Enqueue ackMsg failed, ackMessage: {}; ",
                messageBO);
        }

        if (messageConfig.isEnablePopLog()) {
            log.info("Enqueue ackMsg success, ackMsg: {}, result: {}", messageBO, result);
        }
    }

    private MessageBO buildReviveMsg(PopCheckPointWrapper pointWrapper) {
        return PopConverter.toMessage(
            pointWrapper.getCk(),
            pointWrapper.getReviveQueueId(),
            reviveTopic,
            storeConfig.getHostAddress()
        );
    }

    private void enqueueReviveQueue(PopCheckPointWrapper pointWrapper) {
        if (pointWrapper.getReviveQueueOffset() >= 0) {
            return;
        }

        MessageBO messageBO = buildReviveMsg(pointWrapper);
        EnqueueResult result = enqueueService.enqueue(messageBO);
        if (result.isFailure()) {
            log.error("Enqueue checkpoint failed, checkpoint: {}", pointWrapper);
        }

        pointWrapper.setCkStored(true);
        pointWrapper.setReviveQueueOffset(result.getQueueOffset());

        if (messageConfig.isEnablePopLog()) {
            log.info("Enqueue checkpoint success, checkpoint: {}, result: {}", pointWrapper, result);
        }
    }

}
