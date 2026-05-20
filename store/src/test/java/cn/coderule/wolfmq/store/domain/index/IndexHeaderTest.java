package cn.coderule.wolfmq.store.domain.index;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexHeaderTest {

    @Test
    void testBuilderDefaults() {
        IndexHeader header = IndexHeader.builder().build();
        assertEquals(0, header.getBeginTimestamp());
        assertEquals(0, header.getEndTimestamp());
        assertEquals(0, header.getBeginPhyOffset());
        assertEquals(0, header.getEndPhyOffset());
        assertEquals(0, header.getHashSlotCount());
        assertEquals(0, header.getIndexCount());
    }

    @Test
    void testUpdateTimeDiffFirst() {
        IndexHeader header = IndexHeader.builder().build();
        header.updateTimeDiff(1000L);
        assertEquals(1000L, header.getBeginTimestamp());
        assertEquals(1000L, header.getEndTimestamp());
    }

    @Test
    void testUpdateTimeDiffSubsequent() {
        IndexHeader header = IndexHeader.builder().build();
        header.updateTimeDiff(1000L);
        header.updateTimeDiff(2000L);
        assertEquals(1000L, header.getBeginTimestamp());
        assertEquals(2000L, header.getEndTimestamp());
    }

    @Test
    void testUpdatePhyOffsetFirst() {
        IndexHeader header = IndexHeader.builder().build();
        header.updatePhyOffset(500L);
        assertEquals(500L, header.getBeginPhyOffset());
        assertEquals(500L, header.getEndPhyOffset());
    }

    @Test
    void testUpdatePhyOffsetSubsequent() {
        IndexHeader header = IndexHeader.builder().build();
        header.updatePhyOffset(500L);
        header.updatePhyOffset(1500L);
        assertEquals(500L, header.getBeginPhyOffset());
        assertEquals(1500L, header.getEndPhyOffset());
    }

    @Test
    void testIncrementIndexCount() {
        IndexHeader header = IndexHeader.builder().build();
        assertEquals(0, header.getIndexCount());
        header.incrementIndexCount();
        assertEquals(1, header.getIndexCount());
        header.incrementIndexCount();
        assertEquals(2, header.getIndexCount());
    }

    @Test
    void testHeaderSize() {
        assertEquals(40, IndexHeader.HEADER_SIZE);
    }
}