package cn.coderule.wolfmq.domain.domain.message.utils;

import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cn.coderule.wolfmq.domain.domain.meta.topic.KeyBuilder.POP_ORDER_REVIVE_QUEUE;
import static org.junit.jupiter.api.Assertions.*;

class ExtraInfoUtilsTest {

    @Test
    void split_normal() {
        String extraInfo = "100" + MessageConst.KEY_SEPARATOR + "2000" + MessageConst.KEY_SEPARATOR + "30000"
            + MessageConst.KEY_SEPARATOR + "1" + MessageConst.KEY_SEPARATOR + "0"
            + MessageConst.KEY_SEPARATOR + "brokerA" + MessageConst.KEY_SEPARATOR + "3"
            + MessageConst.KEY_SEPARATOR + "500";
        String[] result = ExtraInfoUtils.split(extraInfo);
        assertEquals(8, result.length);
        assertEquals("100", result[0]);
        assertEquals("2000", result[1]);
    }

    @Test
    void split_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> ExtraInfoUtils.split(null));
    }

    @Test
    void getCkQueueOffset() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals(100L, ExtraInfoUtils.getCkQueueOffset(parts));
    }

    @Test
    void getPopTime() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals(2000L, ExtraInfoUtils.getPopTime(parts));
    }

    @Test
    void getInvisibleTime() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals(30000L, ExtraInfoUtils.getInvisibleTime(parts));
    }

    @Test
    void getReviveQid() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals(1, ExtraInfoUtils.getReviveQid(parts));
    }

    @Test
    void getBrokerName() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals("brokerA", ExtraInfoUtils.getBrokerName(parts));
    }

    @Test
    void getQueueId() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals(3, ExtraInfoUtils.getQueueId(parts));
    }

    @Test
    void getQueueOffset() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals(500L, ExtraInfoUtils.getQueueOffset(parts));
    }

    @Test
    void getRealTopic_normalTopic() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertEquals("myTopic", ExtraInfoUtils.getRealTopic(parts, "myTopic", "myGroup"));
    }

    @Test
    void isOrder_true() {
        String[] parts = {"100", "2000", "30000", String.valueOf(POP_ORDER_REVIVE_QUEUE), "0", "brokerA", "3", "500"};
        assertTrue(ExtraInfoUtils.isOrder(parts));
    }

    @Test
    void isOrder_false() {
        String[] parts = {"100", "2000", "30000", "1", "0", "brokerA", "3", "500"};
        assertFalse(ExtraInfoUtils.isOrder(parts));
    }

    @Test
    void buildExtraInfo_7params() {
        String result = ExtraInfoUtils.buildExtraInfo(100, 2000, 30000, 1, "myTopic", "brokerA", 3);
        assertNotNull(result);
        String[] parts = result.split(MessageConst.KEY_SEPARATOR);
        assertEquals("100", parts[0]);
        assertEquals("2000", parts[1]);
        assertEquals("30000", parts[2]);
        assertEquals("1", parts[3]);
    }

    @Test
    void buildExtraInfo_8params() {
        String result = ExtraInfoUtils.buildExtraInfo(100, 2000, 30000, 1, "myTopic", "brokerA", 3, 500);
        assertNotNull(result);
        String[] parts = result.split(MessageConst.KEY_SEPARATOR);
        assertEquals(8, parts.length);
        assertEquals("500", parts[7]);
    }

    @Test
    void parseStartOffsetInfo_single() {
        String info = "0 3 100";
        Map<String, Long> result = ExtraInfoUtils.parseStartOffsetInfo(info);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get("0@3"));
    }

    @Test
    void parseStartOffsetInfo_multiple() {
        String info = "0 1 10;0 2 20";
        Map<String, Long> result = ExtraInfoUtils.parseStartOffsetInfo(info);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get("0@1"));
        assertEquals(20L, result.get("0@2"));
    }

    @Test
    void parseStartOffsetInfo_null() {
        assertNull(ExtraInfoUtils.parseStartOffsetInfo(null));
        assertNull(ExtraInfoUtils.parseStartOffsetInfo(""));
    }

    @Test
    void parseMsgOffsetInfo_single() {
        String info = "0 3 100,200,300";
        Map<String, List<Long>> result = ExtraInfoUtils.parseMsgOffsetInfo(info);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get("0@3").size());
        assertEquals(100L, result.get("0@3").get(0));
        assertEquals(300L, result.get("0@3").get(2));
    }

    @Test
    void parseOrderCountInfo_single() {
        String info = "0 3 5";
        Map<String, Integer> result = ExtraInfoUtils.parseOrderCountInfo(info);
        assertEquals(1, result.size());
        assertEquals(5, result.get("0@3"));
    }

    @Test
    void parseOrderCountInfo_null() {
        assertNull(ExtraInfoUtils.parseOrderCountInfo(null));
        assertNull(ExtraInfoUtils.parseOrderCountInfo(""));
    }

    @Test
    void parseOrderCountInfo_duplicateThrows() {
        String info = "0 3 5;0 3 10";
        assertThrows(IllegalArgumentException.class, () -> ExtraInfoUtils.parseOrderCountInfo(info));
    }

    @Test
    void getStartOffsetInfoMapKey() {
        assertEquals("0@5", ExtraInfoUtils.getStartOffsetInfoMapKey("topic", 5));
    }

    @Test
    void getQueueOffsetMapKey() {
        assertEquals("0@qo3%100", ExtraInfoUtils.getQueueOffsetMapKey("topic", 3, 100));
    }
}