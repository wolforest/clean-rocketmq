package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.core.enums.store.InsertStatus;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMappedFileQueueTest {

    @Test
    void testCreateMappedFileQueue(@TempDir Path tmpDir) {
        MappedFileQueue queue = new DefaultMappedFileQueue(tmpDir.toString(), 1024);
        queue.load();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        assertNull(queue.getFirstMappedFile());
        assertNull(queue.getLastMappedFile());
        assertNull(queue.getMappedFileByOffset(0));

        assertNotNull(queue.createMappedFileForSize(10));

        queue.destroy();
    }

    @Test
    void testOneFileQueue(@TempDir Path tmpDir) {
        int fileSize = 1024;
        MappedFileQueue queue = new DefaultMappedFileQueue(tmpDir.toString(), fileSize);
        queue.load();
        assertTrue(queue.isEmpty());

        MappedFile mappedFile = queue.createMappedFileForOffset(100);

        // test empty mapped file
        assertEquals(0, mappedFile.getMinOffset());
        assertEquals(fileSize - 1, mappedFile.getMaxOffset());
        assertEquals(0, mappedFile.getInsertPosition());

        byte[] data = "0123456789".getBytes();
        InsertResult insertResult = mappedFile.insert(data);

        // test insert data
        assertEquals(InsertStatus.PUT_OK, insertResult.getStatus());
        assertEquals(0, insertResult.getWroteOffset());
        assertEquals(10, insertResult.getWroteBytes());
        assertEquals(10, mappedFile.getInsertPosition());

        // test insert data again
        insertResult = mappedFile.insert(data);
        assertEquals(InsertStatus.PUT_OK, insertResult.getStatus());
        assertEquals(10, insertResult.getWroteOffset());
        assertEquals(10, insertResult.getWroteBytes());
        assertEquals(20, mappedFile.getInsertPosition());

        // test queue
        MappedFile first = queue.getFirstMappedFile();
        MappedFile last = queue.getLastMappedFile();

        assertEquals(1, queue.size());
        assertNotNull(first);
        assertNotNull(last);
        assertEquals(first.getFileName(), last.getFileName());

        queue.destroy();
    }

    @Test
    void testMultiFileQueue(@TempDir Path tmpDir) {
        int fileSize = 1024;
        MappedFileQueue queue = new DefaultMappedFileQueue(tmpDir.toString(), fileSize);
        queue.load();
        assertTrue(queue.isEmpty());

        MappedFile mappedFile = queue.createMappedFileForOffset(100);
        assertNotNull(mappedFile);

        assertEquals(1, queue.size());
        assertNotNull(queue.getFirstMappedFile());


        mappedFile = queue.createMappedFileForOffset(100 + fileSize);
        assertNotNull(mappedFile);
        assertEquals(2, queue.size());

        mappedFile = queue.createMappedFileForOffset(100 + fileSize * 2);
        assertNotNull(mappedFile);
        assertEquals(3, queue.size());

        mappedFile = queue.createMappedFileForOffset(100 + fileSize * 3);
        assertNotNull(mappedFile);
        assertEquals(4, queue.size());

        queue.destroy();
    }

    @Test
    void testGetMappedFileByOffset(@TempDir Path tmpDir) {
        int fileSize = 1024;
        MappedFileQueue queue = new DefaultMappedFileQueue(tmpDir.toString(), fileSize);
        queue.load();

        // 创建多个文件
        MappedFile file1 = queue.createMappedFileForOffset(0);
        MappedFile file2 = queue.createMappedFileForOffset(fileSize);
        MappedFile file3 = queue.createMappedFileForOffset(fileSize * 2);

        // 测试精确匹配
        assertEquals(file1, queue.getMappedFileByOffset(0));
        assertEquals(file2, queue.getMappedFileByOffset(fileSize));
        assertEquals(file3, queue.getMappedFileByOffset(fileSize * 2));

        // 测试范围匹配
        assertEquals(file1, queue.getMappedFileByOffset(100));
        assertEquals(file2, queue.getMappedFileByOffset(fileSize + 100));
        assertEquals(file3, queue.getMappedFileByOffset(fileSize * 2 + 100));

        // 测试超出范围
        assertNull(queue.getMappedFileByOffset(fileSize * 3));

        queue.destroy();
    }

    @Test
    void testBoundaryConditions(@TempDir Path tmpDir) {
        int fileSize = 1024;
        MappedFileQueue queue = new DefaultMappedFileQueue(tmpDir.toString(), fileSize);
        queue.load();

        // 测试空队列的各种操作
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.getFirstMappedFile());
        assertNull(queue.getLastMappedFile());

        queue.destroy();
    }
}
