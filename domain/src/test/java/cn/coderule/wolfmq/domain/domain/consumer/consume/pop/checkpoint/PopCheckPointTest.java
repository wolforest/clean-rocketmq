package cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopCheckPointTest {

    @Test
    void builderCreatesObjectCorrectly() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .startOffset(100L)
            .popTime(200L)
            .invisibleTime(300L)
            .bitMap(1)
            .num((byte) 5)
            .queueId(1)
            .topic("testTopic")
            .cid("testCid")
            .reviveOffset(400L)
            .brokerName("broker0")
            .build();

        assertEquals(100L, cp.getStartOffset());
        assertEquals(200L, cp.getPopTime());
        assertEquals(300L, cp.getInvisibleTime());
        assertEquals(1, cp.getBitMap());
        assertEquals(5, cp.getNum());
        assertEquals(1, cp.getQueueId());
        assertEquals("testTopic", cp.getTopic());
        assertEquals("testCid", cp.getCId());
        assertEquals(400L, cp.getReviveOffset());
        assertEquals("broker0", cp.getBrokerName());
    }

    @Test
    void getReviveTime_returnsPopTimePlusInvisibleTime() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .popTime(1000L)
            .invisibleTime(5000L)
            .build();

        assertEquals(6000L, cp.getReviveTime());
    }

    @Test
    void addDiff_populatesQueueOffsetDiff() {
        PopCheckPoint cp = PopCheckPoint.builder().build();
        assertNull(cp.getQueueOffsetDiff());

        cp.addDiff(3);
        assertEquals(1, cp.getQueueOffsetDiff().size());
        assertEquals(Integer.valueOf(3), cp.getQueueOffsetDiff().get(0));

        cp.addDiff(7);
        assertEquals(2, cp.getQueueOffsetDiff().size());
        assertEquals(Integer.valueOf(7), cp.getQueueOffsetDiff().get(1));
    }

    @Test
    void indexOfAck_oldVersion_noQueueOffsetDiff() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .startOffset(100L)
            .num((byte) 5)
            .build();

        assertEquals(0, cp.indexOfAck(100L));
        assertEquals(3, cp.indexOfAck(103L));
        assertEquals(4, cp.indexOfAck(104L));
        assertEquals(-1, cp.indexOfAck(105L));
    }

    @Test
    void indexOfAck_newVersion_withQueueOffsetDiff() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .startOffset(100L)
            .num((byte) 5)
            .build();
        cp.addDiff(0);
        cp.addDiff(2);
        cp.addDiff(5);

        assertEquals(0, cp.indexOfAck(100L));
        assertEquals(1, cp.indexOfAck(102L));
        assertEquals(2, cp.indexOfAck(105L));
        assertEquals(-1, cp.indexOfAck(101L));
    }

    @Test
    void indexOfAck_returnsMinusOne_whenAckOffsetLessThanStartOffset() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .startOffset(100L)
            .num((byte) 5)
            .build();

        assertEquals(-1, cp.indexOfAck(99L));
        assertEquals(-1, cp.indexOfAck(0L));
    }

    @Test
    void ackOffsetByIndex_oldVersion_noQueueOffsetDiff() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .startOffset(100L)
            .build();

        assertEquals(100L, cp.ackOffsetByIndex((byte) 0));
        assertEquals(103L, cp.ackOffsetByIndex((byte) 3));
        assertEquals(105L, cp.ackOffsetByIndex((byte) 5));
    }

    @Test
    void ackOffsetByIndex_newVersion_withQueueOffsetDiff() {
        PopCheckPoint cp = PopCheckPoint.builder()
            .startOffset(100L)
            .build();
        cp.addDiff(0);
        cp.addDiff(3);
        cp.addDiff(7);

        assertEquals(100L, cp.ackOffsetByIndex((byte) 0));
        assertEquals(103L, cp.ackOffsetByIndex((byte) 1));
        assertEquals(107L, cp.ackOffsetByIndex((byte) 2));
    }

    @Test
    void compareTo_comparesByStartOffset() {
        PopCheckPoint cp1 = PopCheckPoint.builder().startOffset(100L).build();
        PopCheckPoint cp2 = PopCheckPoint.builder().startOffset(200L).build();
        PopCheckPoint cp3 = PopCheckPoint.builder().startOffset(100L).build();

        assertTrue(cp1.compareTo(cp2) < 0);
        assertTrue(cp2.compareTo(cp1) > 0);
        assertEquals(0, cp1.compareTo(cp3));
    }
}