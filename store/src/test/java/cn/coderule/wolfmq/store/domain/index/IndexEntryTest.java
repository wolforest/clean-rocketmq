package cn.coderule.wolfmq.store.domain.index;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexEntryTest {

    @Test
    void testBuilder() {
        IndexEntry entry = IndexEntry.builder()
            .keyHash(12345)
            .phyOffset(67890L)
            .timeDiff(100)
            .slotValue(0)
            .build();

        assertEquals(12345, entry.getKeyHash());
        assertEquals(67890L, entry.getPhyOffset());
        assertEquals(100, entry.getTimeDiff());
        assertEquals(0, entry.getSlotValue());
    }

    @Test
    void testIndexSize() {
        assertEquals(20, IndexEntry.INDEX_SIZE);
    }

    @Test
    void testNoArgsConstructor() {
        IndexEntry entry = new IndexEntry();
        assertEquals(0, entry.getKeyHash());
        assertEquals(0L, entry.getPhyOffset());
        assertEquals(0, entry.getTimeDiff());
        assertEquals(0, entry.getSlotValue());
    }

    @Test
    void testAllArgsConstructor() {
        IndexEntry entry = new IndexEntry(1, 2L, 3, 4);
        assertEquals(1, entry.getKeyHash());
        assertEquals(2L, entry.getPhyOffset());
        assertEquals(3, entry.getTimeDiff());
        assertEquals(4, entry.getSlotValue());
    }
}