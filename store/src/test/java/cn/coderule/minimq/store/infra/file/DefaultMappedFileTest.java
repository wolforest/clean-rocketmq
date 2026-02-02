package cn.coderule.minimq.store.infra.file;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.minimq.domain.core.enums.store.InsertStatus;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.minimq.domain.domain.store.utils.OffsetUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class DefaultMappedFileTest {

    @TempDir
    Path tmpDir;

    @Test
    void testBasicInsertAndSelect() throws IOException {
        String fileName = createFileName(0);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            // 测试插入数据
            byte[] data = "Hello, World!".getBytes();
            InsertResult result = mappedFile.insert(data);

            assertTrue(result.isSuccess());
            assertEquals(0, result.getWroteOffset());
            assertEquals(data.length, result.getWroteBytes());

            // 测试选择数据
            SelectedMappedBuffer selected = mappedFile.select(0, data.length);
            assertNotNull(selected);
            assertEquals(data.length, selected.getSize());

            ByteBuffer buffer = selected.getByteBuffer();
            byte[] readData = new byte[data.length];
            buffer.get(readData);
            assertArrayEquals(data, readData);

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testMultipleInserts() throws IOException {
        String fileName = createFileName(1000);
        int fileSize = 2048;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            byte[] data1 = "First".getBytes();
            byte[] data2 = "Second".getBytes();

            // 插入第一条数据
            InsertResult result1 = mappedFile.insert(data1);
            assertEquals(InsertStatus.PUT_OK, result1.getStatus());
            assertEquals(0, result1.getWroteOffset());

            // 插入第二条数据
            InsertResult result2 = mappedFile.insert(data2);
            assertEquals(InsertStatus.PUT_OK, result2.getStatus());
            assertEquals(data1.length, result2.getWroteOffset());

            // 验证总位置
            int totalPosition = mappedFile.getInsertPosition();
            assertEquals(data1.length + data2.length, totalPosition);

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testByteBufferInsert() throws IOException {
        String fileName = createFileName(2000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(12345L);
            buffer.putInt(678);
            buffer.flip();

            InsertResult result = mappedFile.insert(buffer);
            assertEquals(InsertStatus.PUT_OK, result.getStatus());
            assertEquals(0, result.getWroteOffset());
            assertEquals(16, result.getWroteBytes());

            // 验证数据
            SelectedMappedBuffer selected = mappedFile.select(0, 16);
            ByteBuffer resultBuffer = selected.getByteBuffer();
            assertEquals(12345L, resultBuffer.getLong());
            assertEquals(678, resultBuffer.getInt());

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testPartialInsert() throws IOException {
        String fileName = createFileName(3000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            byte[] fullData = "0123456789ABCDEF".getBytes();

            // 插入部分数据（从索引4开始，插入8个字节）
            InsertResult result = mappedFile.insert(fullData, 4, 8);
            assertEquals(InsertStatus.PUT_OK, result.getStatus());
            assertEquals(0, result.getWroteOffset());
            assertEquals(8, result.getWroteBytes());

            // 验证插入的数据
            SelectedMappedBuffer selected = mappedFile.select(0, 8);
            ByteBuffer buffer = selected.getByteBuffer();
            byte[] readData = new byte[8];
            buffer.get(readData);

            assertEquals("456789AB", new String(readData));

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testFileBoundary() throws IOException {
        String fileName = createFileName(4000);
        int fileSize = 50; // 小文件测试边界

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            byte[] data20 = new byte[20];
            byte[] data40 = new byte[40];

            // 正常插入
            InsertResult result1 = mappedFile.insert(data20);
            assertTrue(result1.isSuccess());

            // 超出文件大小
            InsertResult result2 = mappedFile.insert(data40);
            assertTrue(result2.isEndOfFile());

            // 测试空间检查
            assertTrue(mappedFile.canWrite(30));
            assertFalse(mappedFile.canWrite(31));

            // 填满文件
            InsertResult result3 = mappedFile.insert(new byte[30]);
            assertTrue(result3.isSuccess());

            assertTrue(mappedFile.isFull());
            assertFalse(mappedFile.canWrite(1));

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testSelectOperations() throws IOException {
        String fileName = createFileName(5000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            byte[] data1 = "Data1".getBytes();
            byte[] data2 = "Data2".getBytes();
            byte[] data3 = "Data3".getBytes();

            mappedFile.insert(data1);
            mappedFile.insert(data2);
            mappedFile.insert(data3);

            // 测试指定大小选择
            SelectedMappedBuffer result1 = mappedFile.select(0, data1.length);
            assertNotNull(result1);
            assertEquals(data1.length, result1.getSize());

            // 测试从位置选择到结尾
            SelectedMappedBuffer result2 = mappedFile.select(data1.length);
            assertNotNull(result2);
            assertEquals(data2.length + data3.length, result2.getSize());

            // 测试超出范围选择
            SelectedMappedBuffer result3 = mappedFile.select(1000, 100);
            assertNull(result3);

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testPositionManagement() throws IOException {
        String fileName = createFileName(6000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            // 初始位置
            assertEquals(0, mappedFile.getWritePosition());
            assertEquals(0, mappedFile.getCommitPosition());
            assertEquals(0, mappedFile.getFlushPosition());

            // 插入数据后
            byte[] data = "Test".getBytes();
            mappedFile.insert(data);
            assertEquals(data.length, mappedFile.getWritePosition());

            // 设置位置
            mappedFile.setWritePosition(100);
            assertEquals(100, mappedFile.getWritePosition());

            mappedFile.setInsertPosition(200);
            assertEquals(200, mappedFile.getWritePosition());
            assertEquals(200, mappedFile.getCommitPosition());
            assertEquals(200, mappedFile.getFlushPosition());

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testFlushAndCommit() throws IOException {
        String fileName = createFileName(7000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            byte[] data = "FlushTest".getBytes();
            mappedFile.insert(data);

            // 测试刷新
            int flushPos = mappedFile.flush(0);
            assertEquals(data.length, flushPos);

            // 测试提交
            int commitPos = mappedFile.commit(0);
            assertEquals(data.length, commitPos);

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testBufferOperations() throws IOException {
        String fileName = createFileName(8000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            // 测试缓冲区获取
            assertNotNull(mappedFile.getMappedByteBuffer());
            assertNotNull(mappedFile.sliceByteBuffer());

            // 测试内存检查
            assertTrue(mappedFile.isInMemory(0, 100));

            // 插入数据
            byte[] data = "BufferTest".getBytes();
            mappedFile.insert(data);

            // 测试切片缓冲区
            ByteBuffer slice = mappedFile.sliceByteBuffer();
            assertNotNull(slice);
            assertTrue(slice.remaining() >= data.length);

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testOffsetContainment() throws IOException {
        String fileName = createFileName(9000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            // 测试偏移量包含（基于文件名推断最小偏移量）
            long minOffset = Long.parseLong(fileName.substring(fileName.lastIndexOf('/') + 1));

            assertTrue(mappedFile.containsOffset(minOffset));
            assertTrue(mappedFile.containsOffset(minOffset + 100));
            assertFalse(mappedFile.containsOffset(minOffset - 1));
            assertFalse(mappedFile.containsOffset(minOffset + fileSize + 1));

            // 测试设置插入偏移量
            mappedFile.setInsertOffset(50);
            assertEquals(50, mappedFile.getInsertPosition());

        } finally {
            mappedFile.destroy();
        }
    }

    @Test
    void testFileDestruction() throws IOException {
        String fileName = createFileName(10000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            // 插入一些数据
            byte[] data = "DestroyTest".getBytes();
            mappedFile.insert(data);

            // 验证文件存在
            assertTrue(FileUtil.exists(fileName));

        } finally {
            // 销毁文件
            mappedFile.destroy();

            // 验证文件被删除
            assertFalse(FileUtil.exists(fileName));
        }
    }

    @Test
    void testFileCreation() throws IOException {
        String fileName = createFileName(11000);
        int fileSize = 1024;

        DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

        try {
            // 验证文件创建
            assertTrue(FileUtil.exists(fileName));

            // 验证初始状态
            assertEquals(0, mappedFile.getInsertPosition());
            assertFalse(mappedFile.isFull());
            assertTrue(mappedFile.canWrite(fileSize));
            assertFalse(mappedFile.canWrite(fileSize + 1));

        } finally {
            mappedFile.destroy();
        }
    }

    private String createFileName(int offset) {
        String fileName = OffsetUtils.offsetToFileName(offset);
        return tmpDir.resolve(fileName).toString();
    }
}
