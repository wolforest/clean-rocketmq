package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.AckBuffer;
import cn.coderule.minimq.domain.domain.consumer.ack.AckMsg;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckMsg;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.pop.checkpoint.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.consumer.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.consumer.pop.helper.PopKeyBuilder;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {
    private final StoreConfig storeConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;

    private final AckBuffer ackBuffer;
    private final MQService MQService;

    public AckService(StoreConfig storeConfig, MQService MQService, String reviveTopic, AckBuffer ackBuffer) {
        this.storeConfig  = storeConfig;
        this.messageConfig  = storeConfig.getMessageConfig();
        this.MQService = MQService;

        this.reviveTopic = reviveTopic;
        this.ackBuffer = ackBuffer;
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

    public void ack(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {
        if (!messageConfig.isEnablePopBufferMerge()) {
            enqueueReviveQueue(ackMsg, reviveQueueId, invisibleTime);
            return;
        }

        try {
            mergeAckMsg(ackMsg, reviveQueueId);
        } catch (Throwable t) {
            log.error("[PopBuffer]ack error, reviveQueueId: {}. ", reviveQueueId, t);
        }
    }

    public long getLatestOffset(String topic, String group, int queueId) {
        String lockKey = KeyBuilder.buildConsumeKey(topic, group, queueId);
        return ackBuffer.getLatestOffset(lockKey);
    }

    public int getTotalSize() {
        return ackBuffer.getTotalSize();
    }

    public int getBufferedSize() {
        return ackBuffer.getCount();
    }

    private PopCheckPointWrapper getCheckPoint(AckMsg ackMsg) {
        String key = PopKeyBuilder.buildKey(ackMsg);
        PopCheckPointWrapper pointWrapper = ackBuffer.getCheckPoint(key);
        if (pointWrapper != null) {
            return pointWrapper;
        }

        if (messageConfig.isEnablePopLog()) {
            log.info("[PopBuffer]can't find ackMsg related PopCheckPointWrapper: {}", ackMsg);
        }

        return null;
    }

    private boolean validateCheckPoint(PopCheckPointWrapper pointWrapper, AckMsg ackMsg, int reviveQueueId) {
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
                    reviveQueueId, pointWrapper, ackMsg, now);
            }
            return false;
        }

        if (now - point.getPopTime() > messageConfig.getPopCkStayBufferTime() - 1500) {
            if (messageConfig.isEnablePopLog()) {
                log.warn("[PopBuffer]add ack fail, rqId={}, stay too long, {}, {}, {}",
                    reviveQueueId, pointWrapper, ackMsg, now);
            }
            return false;
        }

        return true;
    }

    private void mergeAckMsg(AckMsg ackMsg, int reviveQueueId) {
        PopCheckPointWrapper pointWrapper = getCheckPoint(ackMsg);
        if (pointWrapper == null) {
            return;
        }

        if (!validateCheckPoint(pointWrapper, ackMsg, reviveQueueId)) {
            return;
        }

        if (ackMsg instanceof BatchAckMsg) {
            mergeBatchAckMsg((BatchAckMsg) ackMsg, pointWrapper);
            return;
        }

        mergeAckMsg(ackMsg, pointWrapper);
    }

    private void mergeAckMsg(AckMsg ackMsg, PopCheckPointWrapper pointWrapper) {
        mergeByOffset(ackMsg, pointWrapper, ackMsg.getAckOffset());
    }

    private void mergeBatchAckMsg(BatchAckMsg ackMsg, PopCheckPointWrapper pointWrapper) {
        for (Long offset : ackMsg.getAckOffsetList()) {
            mergeByOffset(ackMsg, pointWrapper, offset);
        }
    }

    private void mergeByOffset(AckMsg ackMsg, PopCheckPointWrapper pointWrapper, long offset) {
        int index = pointWrapper.getCk().indexOfAck(offset);
        if (index > -1) {
            markBitCAS(pointWrapper.getBits(), index);
            return;
        }

        log.error("[PopBuffer]invalid index of BatchAckMsg, ackMsg: {}, index: {}, checkPoint: {}",
            ackMsg, index, pointWrapper);
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

    private MessageBO buildAckMsg(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {
        SocketAddress storeHost = new InetSocketAddress(storeConfig.getHost(), storeConfig.getPort());
        return PopConverter.toMessageBO(
            ackMsg,
            reviveQueueId,
            reviveTopic,
            storeHost,
            invisibleTime
        );
    }

    private void enqueueReviveQueue(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {
        MessageBO messageBO = buildAckMsg(ackMsg, reviveQueueId, invisibleTime);
        EnqueueResult result = MQService.enqueue(messageBO);
        if (result.isFailure()) {
            log.error("Enqueue ackMsg failed, ackMsg: {}; reviveQueueId:{}; invisibleTime: {};",
                ackMsg, reviveQueueId, invisibleTime);
        }

        if (messageConfig.isEnablePopLog()) {
            log.info("Enqueue ackMsg success, ackMsg: {}, result: {}", ackMsg, result);
        }
    }

    private MessageBO buildReviveMsg(PopCheckPointWrapper pointWrapper) {
        SocketAddress storeHost = new InetSocketAddress(storeConfig.getHost(), storeConfig.getPort());
        return PopConverter.toMessage(
            pointWrapper.getCk(),
            pointWrapper.getReviveQueueId(),
            reviveTopic,
            storeHost
        );
    }

    private void enqueueReviveQueue(PopCheckPointWrapper pointWrapper) {
        if (pointWrapper.getReviveQueueOffset() >= 0) {
            return;
        }

        MessageBO messageBO = buildReviveMsg(pointWrapper);
        EnqueueResult result = MQService.enqueue(messageBO);
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
