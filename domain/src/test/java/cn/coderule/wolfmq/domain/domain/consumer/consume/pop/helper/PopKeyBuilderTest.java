package cn.coderule.wolfmq.domain.domain.consumer.consume.pop.helper;

import cn.coderule.wolfmq.domain.core.constant.PopConstants;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.BatchAckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PopKeyBuilderTest {

    @Test
    void buildKey_ackInfo() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("myTopic")
            .consumerGroup("myGroup")
            .queueId(1)
            .startOffset(100L)
            .popTime(2000L)
            .brokerName("brokerA")
            .build();
        String result = PopKeyBuilder.buildKey(ackInfo);
        assertEquals("myTopicmyGroup11002000brokerA", result);
    }

    @Test
    void buildKey_checkPoint() {
        PopCheckPoint point = PopCheckPoint.builder()
            .topic("myTopic")
            .cid("myGroup")
            .queueId(1)
            .startOffset(100L)
            .popTime(2000L)
            .brokerName("brokerA")
            .build();
        String result = PopKeyBuilder.buildKey(point);
        assertEquals("myTopicmyGroup11002000brokerA", result);
    }

    @Test
    void buildReviveKey_ackInfo() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("myTopic")
            .consumerGroup("myGroup")
            .queueId(1)
            .startOffset(100L)
            .popTime(2000L)
            .build();
        String result = PopKeyBuilder.buildReviveKey(ackInfo);
        assertEquals("myTopicmyGroup11002000", result);
    }

    @Test
    void buildReviveKey_checkPoint() {
        PopCheckPoint point = PopCheckPoint.builder()
            .topic("myTopic")
            .cid("myGroup")
            .queueId(1)
            .startOffset(100L)
            .popTime(2000L)
            .build();
        String result = PopKeyBuilder.buildReviveKey(point);
        assertEquals("myTopicmyGroup11002000", result);
    }

    @Test
    void genAckUniqueId() {
        AckInfo ackInfo = AckInfo.builder()
            .topic("myTopic")
            .queueId(1)
            .ackOffset(500L)
            .consumerGroup("myGroup")
            .popTime(2000L)
            .brokerName("brokerA")
            .build();
        String result = PopKeyBuilder.genAckUniqueId(ackInfo);
        assertTrue(result.contains("myTopic"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("myGroup"));
        assertTrue(result.contains("2000"));
        assertTrue(result.contains("brokerA"));
        assertTrue(result.endsWith(PopConstants.ACK_TAG));
    }

    @Test
    void genBatchAckUniqueId() {
        BatchAckInfo batchAckInfo = new BatchAckInfo();
        batchAckInfo.setTopic("myTopic");
        batchAckInfo.setQueueId(1);
        batchAckInfo.setAckOffsetList(Arrays.asList(100L, 200L));
        batchAckInfo.setConsumerGroup("myGroup");
        batchAckInfo.setPopTime(2000L);
        String result = PopKeyBuilder.genBatchAckUniqueId(batchAckInfo);
        assertTrue(result.contains("myTopic"));
        assertTrue(result.endsWith(PopConstants.BATCH_ACK_TAG));
    }

    @Test
    void genCkUniqueId() {
        PopCheckPoint point = PopCheckPoint.builder()
            .topic("myTopic")
            .queueId(1)
            .startOffset(100L)
            .cid("myGroup")
            .popTime(2000L)
            .brokerName("brokerA")
            .build();
        String result = PopKeyBuilder.genCkUniqueId(point);
        assertTrue(result.contains("myTopic"));
        assertTrue(result.endsWith(PopConstants.CK_TAG));
    }
}