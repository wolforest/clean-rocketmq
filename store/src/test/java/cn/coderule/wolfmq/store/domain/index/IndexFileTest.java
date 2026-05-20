package cn.coderule.wolfmq.store.domain.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndexFileTest {

    private static final int HASH_SLOT_NUM = 100;
    private static final int INDEX_NUM = 500;

    @TempDir
    File tempDir;

    private IndexFile createIndexFile() throws Exception {
        String fileName = tempDir.getAbsolutePath() + File.separator + System.currentTimeMillis();
        return new IndexFile(fileName, HASH_SLOT_NUM, INDEX_NUM);
    }

    @Test
    void testCreateIndexFile() throws Exception {
        IndexFile indexFile = createIndexFile();
        assertNotNull(indexFile);
        assertEquals(0, indexFile.getBeginTimestamp());
        assertEquals(0, indexFile.getEndTimestamp());
        assertFalse(indexFile.isFull());
    }

    @Test
    void testPutKeySingle() throws Exception {
        IndexFile indexFile = createIndexFile();
        long timestamp = System.currentTimeMillis();
        boolean result = indexFile.putKey("topic#key1", 100L, timestamp);
        assertTrue(result);
        assertEquals(timestamp, indexFile.getBeginTimestamp());
        assertEquals(timestamp, indexFile.getEndTimestamp());
    }

    @Test
    void testPutKeyMultiple() throws Exception {
        IndexFile indexFile = createIndexFile();
        long ts1 = 1000L;
        long ts2 = 2000L;
        indexFile.putKey("topic#key1", 100L, ts1);
        indexFile.putKey("topic#key2", 200L, ts2);

        assertEquals(ts1, indexFile.getBeginTimestamp());
        assertEquals(ts2, indexFile.getEndTimestamp());
    }

    @Test
    void testPutKeyNullReturnsFalse() throws Exception {
        IndexFile indexFile = createIndexFile();
        assertFalse(indexFile.putKey(null, 100L, System.currentTimeMillis()));
    }

    @Test
    void testPutKeyEmptyReturnsFalse() throws Exception {
        IndexFile indexFile = createIndexFile();
        assertFalse(indexFile.putKey("", 100L, System.currentTimeMillis()));
    }

    @Test
    void testSelectPhyOffset() throws Exception {
        IndexFile indexFile = createIndexFile();
        long timestamp = System.currentTimeMillis();
        indexFile.putKey("topic#key1", 100L, timestamp);
        indexFile.putKey("topic#key1", 200L, timestamp + 1000);

        List<Long> offsets = indexFile.selectPhyOffset("topic#key1", 10, 0, 0);
        assertFalse(offsets.isEmpty());
        assertTrue(offsets.contains(100L));
        assertTrue(offsets.contains(200L));
    }

    @Test
    void testSelectPhyOffsetWithTimeRange() throws Exception {
        IndexFile indexFile = createIndexFile();
        long baseTime = System.currentTimeMillis();
        indexFile.putKey("topic#key1", 100L, baseTime);
        indexFile.putKey("topic#key2", 200L, baseTime + 5000);

        List<Long> offsets = indexFile.selectPhyOffset("topic#key1", 10, baseTime - 1000, baseTime + 1000);
        assertFalse(offsets.isEmpty());
        assertEquals(1, offsets.size());
        assertEquals(100L, offsets.get(0));
    }

    @Test
    void testSelectPhyOffsetEmptyKey() throws Exception {
        IndexFile indexFile = createIndexFile();
        List<Long> offsets = indexFile.selectPhyOffset("", 10, 0, 0);
        assertTrue(offsets.isEmpty());
    }

    @Test
    void testSelectPhyOffsetNullKey() throws Exception {
        IndexFile indexFile = createIndexFile();
        List<Long> offsets = indexFile.selectPhyOffset(null, 10, 0, 0);
        assertTrue(offsets.isEmpty());
    }

    @Test
    void testSelectPhyOffsetNotFound() throws Exception {
        IndexFile indexFile = createIndexFile();
        long timestamp = System.currentTimeMillis();
        indexFile.putKey("topic#key1", 100L, timestamp);

        List<Long> offsets = indexFile.selectPhyOffset("topic#nonexistent", 10, 0, 0);
        assertTrue(offsets.isEmpty());
    }

    @Test
    void testIsFull() throws Exception {
        int smallHashSlotNum = 5;
        int smallIndexNum = 3;
        String fileName = tempDir.getAbsolutePath() + File.separator + System.currentTimeMillis();
        IndexFile indexFile = new IndexFile(fileName, smallHashSlotNum, smallIndexNum);

        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < smallIndexNum; i++) {
            indexFile.putKey("topic#key" + i, i * 100L, timestamp + i * 1000);
        }
        assertTrue(indexFile.isFull());

        boolean result = indexFile.putKey("topic#overflow", 999L, timestamp);
        assertFalse(result);
    }

    @Test
    void testScanByTimeRange() throws Exception {
        IndexFile indexFile = createIndexFile();
        long baseTime = 1000000L;
        indexFile.putKey("topic#key1", 100L, baseTime);
        indexFile.putKey("topic#key2", 200L, baseTime + 5000);
        indexFile.putKey("topic#key3", 300L, baseTime + 10000);

        List<Long> offsets = indexFile.scanByTimeRange(baseTime - 1000, baseTime + 6000);
        assertEquals(2, offsets.size());
        assertTrue(offsets.contains(100L));
        assertTrue(offsets.contains(200L));
    }

    @Test
    void testFlushDoesNotThrow() throws Exception {
        IndexFile indexFile = createIndexFile();
        indexFile.putKey("topic#key1", 100L, System.currentTimeMillis());
        assertDoesNotThrow(indexFile::flush);
    }
}