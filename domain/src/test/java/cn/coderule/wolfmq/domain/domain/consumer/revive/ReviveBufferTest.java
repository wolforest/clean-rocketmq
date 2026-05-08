package cn.coderule.wolfmq.domain.domain.consumer.revive;

import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.helper.PopKeyBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviveBufferTest {

    private PopCheckPoint createCheckPoint(String topic, String cid, int queueId, long startOffset, long popTime, long reviveOffset) {
        return PopCheckPoint.builder()
            .topic(topic)
            .cid(cid)
            .queueId(queueId)
            .startOffset(startOffset)
            .popTime(popTime)
            .reviveOffset(reviveOffset)
            .build();
    }

    @Test
    void initialOffset() {
        ReviveBuffer buffer = new ReviveBuffer(100L);
        assertEquals(100L, buffer.getInitialOffset());
    }

    @Test
    void addAndGetCheckPoint() {
        ReviveBuffer buffer = new ReviveBuffer(0L);
        PopCheckPoint point = createCheckPoint("topic", "group", 0, 100L, 2000L, 300L);
        buffer.addCheckPoint(point);
        String key = PopKeyBuilder.buildKey(point);
        assertNotNull(buffer.getCheckPoint(key));
        assertEquals(1, buffer.getCheckPointMap().size());
    }

    @Test
    void addAck_and_getSortedList() {
        ReviveBuffer buffer = new ReviveBuffer(0L);
        PopCheckPoint point1 = createCheckPoint("topic", "group", 0, 100L, 2000L, 300L);
        PopCheckPoint point2 = createCheckPoint("topic", "group", 0, 200L, 2000L, 100L);

        buffer.addCheckPoint(point1);
        buffer.addCheckPoint(point2);

        var sorted = buffer.getSortedList();
        assertNotNull(sorted);
        assertEquals(2, sorted.size());
        assertTrue(sorted.get(0).getReviveOffset() <= sorted.get(1).getReviveOffset());
    }

    @Test
    void addAck_and_mergeAckMap() {
        ReviveBuffer buffer = new ReviveBuffer(0L);
        PopCheckPoint point = createCheckPoint("topic", "group", 0, 100L, 2000L, 300L);
        PopCheckPoint ackPoint = createCheckPoint("topic", "group", 0, 150L, 2000L, 500L);
        buffer.addCheckPoint(point);
        buffer.addAck("ackKey", ackPoint);

        assertEquals(1, buffer.getAckMap().size());
        assertEquals(1, buffer.getCheckPointMap().size());

        buffer.mergeAckMap();
        assertEquals(2, buffer.getCheckPointMap().size());
    }

    @Test
    void increaseNoMsgCount() {
        ReviveBuffer buffer = new ReviveBuffer(0L);
        assertEquals(0, buffer.getNoMsgCount());
        buffer.increaseNoMsgCount();
        assertEquals(1, buffer.getNoMsgCount());
        buffer.increaseNoMsgCount();
        assertEquals(2, buffer.getNoMsgCount());
    }

    @Test
    void offset_startsAtInitialPlusOne() {
        ReviveBuffer buffer = new ReviveBuffer(100L);
        assertEquals(101L, buffer.getOffset());
    }
}