package cn.coderule.wolfmq.store.domain.commitlog.it;

import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.SelectedMappedBuffer;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CommitLogIntegrationTest extends BaseCommitLogIntegrationTest {

    private static final int SHARD_ID = 0;

    @Test
    void testInsertAndSelect() {
        byte[] data = "commitlog integration test data".getBytes(StandardCharsets.UTF_8);
        InsertResult result = commitLogStore.insert(0, data, 0, data.length);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getWroteOffset() >= 0);

        SelectedMappedBuffer buffer = commitLogStore.select(result.getWroteOffset());
        assertNotNull(buffer);

        byte[] readData = new byte[data.length];
        buffer.getByteBuffer().get(readData);
        assertArrayEquals(data, readData);

        buffer.release();
    }

    @Test
    void testMinMaxOffsetTracking() {
        assertEquals(0, commitLogStore.getMinOffset(SHARD_ID));
        assertEquals(0, commitLogStore.getMaxOffset(SHARD_ID));

        byte[] data = new byte[128];
        commitLogStore.insert(0, data, 0, data.length);

        assertEquals(0, commitLogStore.getMinOffset(SHARD_ID));
        assertEquals(data.length, commitLogStore.getMaxOffset(SHARD_ID));
    }

    @Test
    void testLargeDataInsert() {
        byte[] largeData = new byte[8192];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        InsertResult result = commitLogStore.insert(0, largeData, 0, largeData.length);
        assertTrue(result.isSuccess());

        SelectedMappedBuffer buffer = commitLogStore.select(0);
        assertNotNull(buffer);
        byte[] actual = new byte[largeData.length];
        buffer.getByteBuffer().get(actual);
        assertArrayEquals(largeData, actual);
        buffer.release();
    }

    @Test
    void testFlushedOffsetAndUnFlushedSize() {
        assertEquals(0, commitLogStore.getFlushedOffset(SHARD_ID));

        byte[] data = new byte[256];
        InsertResult result = commitLogStore.insert(0, data, 0, data.length);
        assertTrue(result.isSuccess());

        long unFlushedSize = commitLogStore.getUnFlushedSize(SHARD_ID);
        assertTrue(unFlushedSize > 0, "unFlushedSize should be positive after insert, got: " + unFlushedSize);

        long maxOffset = commitLogStore.getMaxOffset(SHARD_ID);
        assertTrue(maxOffset > 0, "maxOffset should be positive after insert");
    }

    @Test
    void testSequentialInsertsIncrementOffset() {
        byte[] dataA = "dataA".getBytes(StandardCharsets.UTF_8);
        byte[] dataB = "dataB".getBytes(StandardCharsets.UTF_8);
        byte[] dataC = "dataC".getBytes(StandardCharsets.UTF_8);

        InsertResult resultA = commitLogStore.insert(0, dataA, 0, dataA.length);
        assertTrue(resultA.isSuccess());
        long offsetA = resultA.getWroteOffset();

        long maxAfterA = commitLogStore.getMaxOffset(SHARD_ID);
        assertTrue(maxAfterA > 0);

        InsertResult resultB = commitLogStore.insert(maxAfterA, dataB, 0, dataB.length);
        assertTrue(resultB.isSuccess());
        long offsetB = resultB.getWroteOffset();
        assertTrue(offsetB > offsetA, "second insert offset should be greater than first");

        long maxAfterB = commitLogStore.getMaxOffset(SHARD_ID);
        assertTrue(maxAfterB > maxAfterA, "maxOffset should increase after second insert");

        InsertResult resultC = commitLogStore.insert(maxAfterB, dataC, 0, dataC.length);
        assertTrue(resultC.isSuccess());
        long offsetC = resultC.getWroteOffset();
        assertTrue(offsetC > offsetB, "third insert offset should be greater than second");

        assertEquals(0, commitLogStore.getMinOffset(SHARD_ID));
    }

    @Test
    void testSelectAtMiddleOffset() {
        byte[] dataA = "AAAA".getBytes(StandardCharsets.UTF_8);
        byte[] dataB = "BBBB".getBytes(StandardCharsets.UTF_8);

        InsertResult resultA = commitLogStore.insert(0, dataA, 0, dataA.length);
        assertTrue(resultA.isSuccess());
        long offsetA = resultA.getWroteOffset();

        long maxAfterA = commitLogStore.getMaxOffset(SHARD_ID);

        InsertResult resultB = commitLogStore.insert(maxAfterA, dataB, 0, dataB.length);
        assertTrue(resultB.isSuccess());
        long offsetB = resultB.getWroteOffset();

        SelectedMappedBuffer bufferB = commitLogStore.select(offsetB);
        assertNotNull(bufferB, "should be able to select data at middle offset");
        byte[] readB = new byte[dataB.length];
        bufferB.getByteBuffer().get(readB);
        assertArrayEquals(dataB, readB, "data at middle offset should match");
        bufferB.release();

        SelectedMappedBuffer bufferA = commitLogStore.select(offsetA);
        assertNotNull(bufferA, "should be able to select data at first offset");
        byte[] readA = new byte[dataA.length];
        bufferA.getByteBuffer().get(readA);
        assertArrayEquals(dataA, readA);
        bufferA.release();
    }

    @Test
    void testPartialDataInsert() {
        byte[] fullData = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        int start = 4;
        int size = 8;
        byte[] expectedSlice = new byte[size];
        System.arraycopy(fullData, start, expectedSlice, 0, size);

        InsertResult result = commitLogStore.insert(0, fullData, start, size);
        assertTrue(result.isSuccess());

        SelectedMappedBuffer buffer = commitLogStore.select(result.getWroteOffset());
        assertNotNull(buffer);

        byte[] readData = new byte[size];
        buffer.getByteBuffer().get(readData);
        assertArrayEquals(expectedSlice, readData, "partial data (offset+size) should match");
        buffer.release();
    }

    @Test
    void testSelectNonExistentOffsetReturnsNull() {
        SelectedMappedBuffer buffer = commitLogStore.select(999999);
        assertNull(buffer, "select at non-existent offset should return null");
    }

    @Test
    void testInsertResultFields() {
        byte[] data = "field validation".getBytes(StandardCharsets.UTF_8);
        InsertResult result = commitLogStore.insert(0, data, 0, data.length);

        assertTrue(result.isSuccess());
        assertTrue(result.getWroteOffset() >= 0);
        assertEquals(data.length, result.getWroteBytes());
    }
}
