package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.core.enums.store.InsertStatus;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class MultiDirMappedFileQueueTest {

    private static final int FILE_SIZE = 1024;

    private List<String> createDirs(Path tmpDir, int count) {
        List<String> dirs = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Path dir = tmpDir.resolve("dir" + i);
            dir.toFile().mkdirs();
            dirs.add(dir.toString());
        }
        return dirs;
    }

    // ==================== Constructor ====================

    @Test
    void testConstructor_NullDirs() {
        assertThrows(IllegalArgumentException.class,
            () -> new MultiDirMappedFileQueue(null, FILE_SIZE));
    }

    @Test
    void testConstructor_EmptyDirs() {
        assertThrows(IllegalArgumentException.class,
            () -> new MultiDirMappedFileQueue(List.of(), FILE_SIZE));
    }

    // ==================== Lifecycle ====================

    @Test
    void testLoad(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);

        assertTrue(queue.load());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        queue.destroy();
    }

    @Test
    void testDestroy(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.getOrCreateMappedFileForSize(10);
        queue.getOrCreateMappedFileForSize(10);
        assertFalse(queue.isEmpty());

        queue.destroy();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testShutdown(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.getOrCreateMappedFileForSize(10);
        assertDoesNotThrow(() -> queue.shutdown(1000));

        queue.destroy();
    }

    @Test
    void testCheckSelf(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.getOrCreateMappedFileForSize(10);
        assertDoesNotThrow(() -> queue.checkSelf());

        queue.destroy();
    }

    // ==================== Empty Queue ====================

    @Test
    void testEmptyQueue(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.getFirstMappedFile());
        assertNull(queue.getLastMappedFile());
        assertNull(queue.getMappedFileByOffset(0));
        assertNull(queue.getMappedFileByIndex(0));
        assertEquals(0, queue.getMinOffset());
        assertEquals(0, queue.getMaxOffset());
        assertEquals(0, queue.getFlushPosition());
        assertEquals(0, queue.getCommitPosition());

        queue.destroy();
    }

    // ==================== Create & Get ====================

    @Test
    void testCreateMappedFileByStartOffset(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file = queue.createMappedFileByStartOffset(0);
        assertNotNull(file);
        assertEquals(0, file.getMinOffset());
        assertEquals(1, queue.size());

        queue.destroy();
    }

    @Test
    void testGetOrCreateMappedFileForSize(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file1 = queue.getOrCreateMappedFileForSize(100);
        assertNotNull(file1);
        assertEquals(1, queue.size());

        // same file returned when space available
        MappedFile file2 = queue.getOrCreateMappedFileForSize(100);
        assertEquals(file1, file2);
        assertEquals(1, queue.size());

        queue.destroy();
    }

    @Test
    void testGetOrCreateMappedFileForOffset(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file1 = queue.getOrCreateMappedFileForOffset(0);
        assertNotNull(file1);
        assertEquals(0, file1.getMinOffset());

        MappedFile file2 = queue.getOrCreateMappedFileForOffset(FILE_SIZE);
        assertNotNull(file2);
        assertEquals(FILE_SIZE, file2.getMinOffset());
        assertEquals(2, queue.size());

        queue.destroy();
    }

    // ==================== Round-Robin Distribution ====================

    @Test
    void testRoundRobinDistribution(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 3);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        // create 6 files, should distribute 2 per directory
        for (int i = 0; i < 6; i++) {
            queue.createMappedFileByStartOffset((long) i * FILE_SIZE);
        }

        assertEquals(6, queue.size());

        // each sub-queue should have 2 files
        for (DefaultMappedFileQueue subQueue : queue.getQueues()) {
            assertEquals(2, subQueue.size());
        }

        queue.destroy();
    }

    // ==================== First & Last ====================

    @Test
    void testGetFirstAndLastMappedFile(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        // dir0 gets offset 0, dir1 gets offset 1024, dir0 gets offset 2048
        queue.createMappedFileByStartOffset(0);
        queue.createMappedFileByStartOffset(FILE_SIZE);
        queue.createMappedFileByStartOffset(FILE_SIZE * 2L);

        MappedFile first = queue.getFirstMappedFile();
        MappedFile last = queue.getLastMappedFile();

        assertNotNull(first);
        assertNotNull(last);
        assertEquals(0, first.getMinOffset());
        assertEquals(FILE_SIZE * 2L, last.getMinOffset());

        queue.destroy();
    }

    // ==================== GetMappedFiles (sorted) ====================

    @Test
    void testGetMappedFiles_Sorted(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.createMappedFileByStartOffset(0);
        queue.createMappedFileByStartOffset(FILE_SIZE);
        queue.createMappedFileByStartOffset(FILE_SIZE * 2L);

        List<MappedFile> all = queue.getMappedFiles();
        assertEquals(3, all.size());

        // verify sorted by offset
        for (int i = 1; i < all.size(); i++) {
            assertTrue(all.get(i).getMinOffset() > all.get(i - 1).getMinOffset());
        }

        queue.destroy();
    }

    // ==================== GetMappedFileByIndex ====================

    @Test
    void testGetMappedFileByIndex(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.createMappedFileByStartOffset(0);
        queue.createMappedFileByStartOffset(FILE_SIZE);
        queue.createMappedFileByStartOffset(FILE_SIZE * 2L);

        MappedFile f0 = queue.getMappedFileByIndex(0);
        MappedFile f1 = queue.getMappedFileByIndex(1);
        MappedFile f2 = queue.getMappedFileByIndex(2);

        assertNotNull(f0);
        assertNotNull(f1);
        assertNotNull(f2);
        assertEquals(0, f0.getMinOffset());
        assertEquals(FILE_SIZE, f1.getMinOffset());
        assertEquals(FILE_SIZE * 2L, f2.getMinOffset());

        // out of range
        assertNull(queue.getMappedFileByIndex(3));
        assertNull(queue.getMappedFileByIndex(-1));

        queue.destroy();
    }

    // ==================== GetMappedFileByOffset ====================

    @Test
    void testGetMappedFileByOffset(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile f1 = queue.createMappedFileByStartOffset(0);
        MappedFile f2 = queue.createMappedFileByStartOffset(FILE_SIZE);

        assertEquals(f1, queue.getMappedFileByOffset(0));
        assertEquals(f1, queue.getMappedFileByOffset(100));
        assertEquals(f2, queue.getMappedFileByOffset(FILE_SIZE));
        assertEquals(f2, queue.getMappedFileByOffset(FILE_SIZE + 100));

        queue.destroy();
    }

    // ==================== Remove ====================

    @Test
    void testRemoveMappedFile(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file = queue.createMappedFileByStartOffset(0);
        assertEquals(1, queue.size());

        queue.removeMappedFile(file);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());

        queue.destroy();
    }

    @Test
    void testRemoveMappedFiles(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile f1 = queue.createMappedFileByStartOffset(0);
        MappedFile f2 = queue.createMappedFileByStartOffset(FILE_SIZE);
        assertEquals(2, queue.size());

        queue.removeMappedFiles(List.of(f1, f2));
        assertTrue(queue.isEmpty());

        queue.destroy();
    }

    @Test
    void testRemoveNull(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.createMappedFileByStartOffset(0);
        assertDoesNotThrow(() -> queue.removeMappedFile(null));
        assertEquals(1, queue.size());

        queue.destroy();
    }

    // ==================== Insert & Offset ====================

    @Test
    void testInsertAndOffset(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file = queue.getOrCreateMappedFileForSize(100);
        byte[] data = "Hello, Multi-Dir!".getBytes();
        InsertResult result = file.insert(data);

        assertEquals(InsertStatus.PUT_OK, result.getStatus());
        assertEquals(0, queue.getMinOffset());
        assertEquals(data.length, queue.getMaxOffset());

        queue.destroy();
    }

    // ==================== Flush ====================

    @Test
    void testFlushEmptyQueue(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        assertTrue(queue.flush(0));
        assertEquals(0, queue.getFlushPosition());

        queue.destroy();
    }

    @Test
    void testFlush(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file = queue.getOrCreateMappedFileForSize(100);
        file.insert("flush test".getBytes());

        queue.flush(0);
        assertTrue(queue.getFlushPosition() > 0);
        assertTrue(queue.getStoreTimestamp() > 0);

        queue.destroy();
    }

    // ==================== Commit ====================

    @Test
    void testCommitEmptyQueue(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        assertTrue(queue.commit(0));
        assertEquals(0, queue.getCommitPosition());

        queue.destroy();
    }

    @Test
    void testCommit(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file = queue.getOrCreateMappedFileForSize(100);
        file.insert("commit test".getBytes());

        queue.commit(0);
        assertTrue(queue.getCommitPosition() > 0);

        queue.destroy();
    }

    // ==================== UnFlushed & UnCommitted ====================

    @Test
    void testUnFlushedAndUnCommittedSize(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile file = queue.getOrCreateMappedFileForSize(100);
        byte[] data = "dirty data".getBytes();
        file.insert(data);

        assertTrue(queue.getUnFlushedSize() > 0);
        assertTrue(queue.getUnCommittedSize() > 0);

        queue.flush(0);
        queue.commit(0);

        assertEquals(0, queue.getUnFlushedSize());
        assertEquals(0, queue.getUnCommittedSize());

        queue.destroy();
    }

    // ==================== SetFileMode ====================

    @Test
    void testSetFileMode(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 2);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        queue.getOrCreateMappedFileForSize(10);
        assertDoesNotThrow(() -> queue.setFileMode(0));

        queue.destroy();
    }

    // ==================== Multi-Dir Insert Flow ====================

    @Test
    void testMultiDirInsertFlow(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 3);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        // fill first file
        MappedFile f1 = queue.getOrCreateMappedFileForSize(FILE_SIZE);
        assertNotNull(f1);
        f1.insert(new byte[FILE_SIZE]);
        assertTrue(f1.isFull());

        // next file should be in a different directory
        MappedFile f2 = queue.getOrCreateMappedFileForSize(100);
        assertNotNull(f2);
        assertNotEquals(f1, f2);
        assertEquals(FILE_SIZE, f2.getMinOffset());
        assertEquals(2, queue.size());

        // verify files are in different sub-queues
        int subQueueWithFiles = 0;
        for (DefaultMappedFileQueue subQueue : queue.getQueues()) {
            if (!subQueue.isEmpty()) {
                subQueueWithFiles++;
            }
        }
        assertEquals(2, subQueueWithFiles);

        queue.destroy();
    }

    // ==================== Single Dir Fallback ====================

    @Test
    void testSingleDir(@TempDir Path tmpDir) {
        List<String> dirs = createDirs(tmpDir, 1);
        MultiDirMappedFileQueue queue = new MultiDirMappedFileQueue(dirs, FILE_SIZE);
        queue.load();

        MappedFile f1 = queue.createMappedFileByStartOffset(0);
        MappedFile f2 = queue.createMappedFileByStartOffset(FILE_SIZE);

        assertNotNull(f1);
        assertNotNull(f2);
        assertEquals(2, queue.size());

        // single sub-queue has all files
        assertEquals(2, queue.getQueues().get(0).size());

        queue.destroy();
    }
}
