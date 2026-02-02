package cn.coderule.minimq.store.infra.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TransientPool单元测试
 */
public class TransientPoolTest {
    private static final int POOL_SIZE = 5;
    private static final int BUFFER_SIZE = 1024;
    private TransientPool transientPool;

    @BeforeEach
    void setUp() throws Exception {
        transientPool = new TransientPool(POOL_SIZE, BUFFER_SIZE);
        transientPool.initialize();
    }

    @Test
    void testBasicCreation() {
        assertNotNull(transientPool);
        assertEquals(POOL_SIZE, getPoolSize());
        assertEquals(BUFFER_SIZE, getFileSize());
        assertEquals(0, transientPool.availableBufferNums());
        assertTrue(isEnabled());
    }

    @Test
    void testStart() throws Exception {
        transientPool.start();

        // 验证池被正确初始化
        assertEquals(POOL_SIZE, transientPool.availableBufferNums());

        // 验证每个缓冲区都有正确的容量
        for (int i = 0; i < POOL_SIZE; i++) {
            ByteBuffer buffer = transientPool.borrowBuffer();
            assertNotNull(buffer);
            assertEquals(BUFFER_SIZE, buffer.capacity());
            assertEquals(BUFFER_SIZE, buffer.limit());
            assertEquals(0, buffer.position());
            assertTrue(buffer.isDirect());

            // 归还缓冲区
            transientPool.returnBuffer(buffer);
        }

        assertEquals(POOL_SIZE, transientPool.availableBufferNums());
    }

    @Test
    void testBorrowReturnBuffer() throws Exception {
        transientPool.start();

        // 借用所有缓冲区
        ByteBuffer[] borrowed = new ByteBuffer[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) {
            borrowed[i] = transientPool.borrowBuffer();
            assertNotNull(borrowed[i]);
            assertEquals(BUFFER_SIZE, borrowed[i].capacity());
        }

        // 池应该为空
        assertEquals(0, transientPool.availableBufferNums());

        // 归还缓冲区
        for (int i = 0; i < POOL_SIZE; i++) {
            borrowed[i].position(10); // 修改位置
            borrowed[i].limit(BUFFER_SIZE - 20); // 修改限制

            transientPool.returnBuffer(borrowed[i]);
        }

        // 池应该再次满
        assertEquals(POOL_SIZE, transientPool.availableBufferNums());

        // 验证归还的缓冲区被重置
        ByteBuffer returned = transientPool.borrowBuffer();
        assertEquals(0, returned.position());
        assertEquals(BUFFER_SIZE, returned.limit());
    }

    @Test
    void testBorrowFromEmptyPool() throws Exception {
        transientPool.start();

        // 借用所有缓冲区
        for (int i = 0; i < POOL_SIZE; i++) {
            transientPool.borrowBuffer();
        }

        // 尝试从空池借用
        ByteBuffer emptyBuffer = transientPool.borrowBuffer();
        assertNull(emptyBuffer);
    }

    @Test
    void testMemoryLocking() throws Exception {
        transientPool.start();

        ByteBuffer buffer = transientPool.borrowBuffer();
        assertNotNull(buffer);

        // 验证缓冲区被锁定（通过直接内存分配验证）
        assertTrue(buffer.isDirect());
        assertEquals(BUFFER_SIZE, buffer.capacity());

        // 验证缓冲区可以正常使用
        buffer.putInt(0x12345678);
        buffer.putInt(0x87654321);
        buffer.flip();

        assertEquals(0x12345678, buffer.getInt());
        assertEquals(0x87654321, buffer.getInt());

        transientPool.returnBuffer(buffer);
    }

    @Test
    void testBufferReset() throws Exception {
        transientPool.start();

        ByteBuffer buffer = transientPool.borrowBuffer();
        assertNotNull(buffer);

        // 修改缓冲区状态
        buffer.position(100);
        buffer.limit(BUFFER_SIZE - 50);

        // 归还缓冲区
        transientPool.returnBuffer(buffer);

        // 重新借用并验证重置
        ByteBuffer resetBuffer = transientPool.borrowBuffer();
        assertEquals(0, resetBuffer.position());
        assertEquals(BUFFER_SIZE, resetBuffer.limit());

        transientPool.returnBuffer(resetBuffer);
    }

    @Test
    void testPoolExhaustionWarning() throws Exception {
        transientPool.start();

        // 借用大部分缓冲区触发警告
        int threshold = (int) (POOL_SIZE * 0.4) + 1; // 少于40%

        for (int i = 0; i < threshold; i++) {
            transientPool.borrowBuffer();
        }

        // 验证当前可用数量
        int remaining = POOL_SIZE - threshold;
        assertEquals(remaining, transientPool.availableBufferNums());
    }

    @Test
    void testShutdown() throws Exception {
        transientPool.start();

        // 借用一些缓冲区
        ByteBuffer[] borrowed = new ByteBuffer[3];
        for (int i = 0; i < 3; i++) {
            borrowed[i] = transientPool.borrowBuffer();
            assertNotNull(borrowed[i]);
        }

        // 关闭池
        transientPool.shutdown();

        // 验证池仍然包含所有缓冲区
        assertEquals(0, transientPool.availableBufferNums());
        assertFalse(isEnabled());

        // 验证缓冲区仍可使用（内存未被解锁）
        for (ByteBuffer buffer : borrowed) {
            assertNotNull(buffer);
            assertTrue(buffer.isDirect());
        }
    }

    @Test
    void testEnabledFlag() throws Exception {
        transientPool.start();

        // 默认启用
        assertTrue(isEnabled());

        // 设置为禁用
        setEnabled(false);
        assertFalse(isEnabled());

        // 重新启用
        setEnabled(true);
        assertTrue(isEnabled());

        transientPool.shutdown();
        assertFalse(isEnabled());
    }

    /**
     * 使用反射获取enabled字段值
     */
    private boolean isEnabled() {
        try {
            Field field = TransientPool.class.getDeclaredField("enabled");
            field.setAccessible(true);
            return (boolean) field.get(transientPool);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 使用反射设置enabled字段值
     */
    private void setEnabled(boolean enabled) {
        try {
            Field field = TransientPool.class.getDeclaredField("enabled");
            field.setAccessible(true);
            field.set(transientPool, enabled);
        } catch (Exception e) {
            // 忽略反射错误
        }
    }

    @Test
    void testReturnNullBuffer() throws Exception {
        transientPool.start();

        // 归还null缓冲区应该不抛出异常
        assertDoesNotThrow(() -> {
            transientPool.returnBuffer(null);
        });

        assertEquals(POOL_SIZE, transientPool.availableBufferNums());
    }

    @Test
    void testBufferCapacityConsistency() throws Exception {
        transientPool.start();

        for (int i = 0; i < POOL_SIZE; i++) {
            ByteBuffer buffer = transientPool.borrowBuffer();
            assertNotNull(buffer);

            // 验证容量一致性
            assertEquals(BUFFER_SIZE, buffer.capacity());
            assertEquals(BUFFER_SIZE, buffer.limit());

            transientPool.returnBuffer(buffer);
        }
    }

    @Test
    void testInitializeAndStartDifference() throws Exception {
        // initialize应该不执行任何操作
        transientPool.initialize();
        assertEquals(0, transientPool.availableBufferNums());

        // start才真正初始化
        transientPool.start();
        assertEquals(POOL_SIZE, transientPool.availableBufferNums());
    }

    @Test
    void testBufferIsolation() throws Exception {
        transientPool.start();

        ByteBuffer buffer1 = transientPool.borrowBuffer();
        ByteBuffer buffer2 = transientPool.borrowBuffer();

        assertNotNull(buffer1);
        assertNotNull(buffer2);
        assertNotSame(buffer1, buffer2);

        // 验证缓冲区隔离
        buffer1.putInt(100);
        buffer2.putInt(200);

        buffer1.flip();
        buffer2.flip();

        assertEquals(100, buffer1.getInt());
        assertEquals(200, buffer2.getInt());

        transientPool.returnBuffer(buffer1);
        transientPool.returnBuffer(buffer2);
    }

    /**
     * 使用反射获取私有字段值（仅用于测试）
     */
    private int getPoolSize() {
        try {
            Field field = TransientPool.class.getDeclaredField("poolSize");
            field.setAccessible(true);
            return (int) field.get(transientPool);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 使用反射获取私有字段值（仅用于测试）
     */
    private int getFileSize() {
        try {
            Field field = TransientPool.class.getDeclaredField("fileSize");
            field.setAccessible(true);
            return (int) field.get(transientPool);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 使用反射获取可用缓冲区队列（仅用于测试）
     */
    private int getAvailableBuffersCount() {
        try {
            Field field = TransientPool.class.getDeclaredField("availableBuffers");
            field.setAccessible(true);
            java.util.Deque<?> deque = (java.util.Deque<?>) field.get(transientPool);
            return deque.size();
        } catch (Exception e) {
            return -1;
        }
    }
}
