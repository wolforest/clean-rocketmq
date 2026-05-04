package cn.coderule.wolfmq.domain.domain.meta.order;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderUtilsTest {

    @Test
    void buildKey() {
        assertEquals("myTopic@myGroup", OrderUtils.buildKey("myTopic", "myGroup"));
    }

    @Test
    void decodeKey() {
        String[] parts = OrderUtils.decodeKey("myTopic@myGroup");
        assertEquals(2, parts.length);
        assertEquals("myTopic", parts[0]);
        assertEquals("myGroup", parts[1]);
    }

    @Test
    void buildOffsetList_singleOffset() {
        List<Long> offsets = Collections.singletonList(100L);
        List<Long> result = OrderUtils.buildOffsetList(offsets);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0));
    }

    @Test
    void buildOffsetList_multipleOffsets_calculatesDiffs() {
        List<Long> offsets = Arrays.asList(100L, 105L, 110L, 120L);
        List<Long> result = OrderUtils.buildOffsetList(offsets);
        assertEquals(4, result.size());
        assertEquals(100L, result.get(0));
        assertEquals(5L, result.get(1));
        assertEquals(10L, result.get(2));
        assertEquals(20L, result.get(3));
    }

    @Test
    void buildOffsetList_twoOffsets() {
        List<Long> offsets = Arrays.asList(50L, 75L);
        List<Long> result = OrderUtils.buildOffsetList(offsets);
        assertEquals(2, result.size());
        assertEquals(50L, result.get(0));
        assertEquals(25L, result.get(1));
    }
}