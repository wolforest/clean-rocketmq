package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.infra.MappedFile;
import cn.coderule.minimq.store.infra.memory.TransientPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AllocateMappedFileService 单元测试（不使用Mock）
 */
public class AllocateMappedFileServiceTest {

    @TempDir
    File tempDir;

    private StoreConfig mockStoreConfig;
    private TransientPool mockTransientPool;
    private AllocateMappedFileService service;

    @BeforeEach
    void setUp() {
        // 创建简单的 mock 实现
        mockStoreConfig = new StoreConfig();

        mockStoreConfig.setEnableTransientPool(true);
        mockStoreConfig.setFastFailIfNotExistInTransientPool(false);

        mockTransientPool = new TransientPool(10240, 1024); // 10 buffers of 1KB each

        service = new AllocateMappedFileService(mockStoreConfig, mockTransientPool);
    }

    @Test
    void testServiceInitialization() throws Exception {
        assertNotNull(service);
        assertEquals("AllocateMappedFileService", service.getServiceName());
        assertFalse(service.isStopped());

        service.initialize();
        service.shutdown();
    }

    @Test
    void testEnqueueFastFailScenario() {
        mockStoreConfig = new StoreConfig() {
            @Override
            public boolean isEnableTransientPool() {
                return true;
            }

            @Override
            public boolean isFastFailIfNotExistInTransientPool() {
                return true;
            }
        };

        mockTransientPool = new TransientPool(0, 1024); // No buffers available

        service = new AllocateMappedFileService(mockStoreConfig, mockTransientPool);

        String filePath = tempDir.getAbsolutePath() + File.separator + "fail_test.mmap";
        String nextFilePath = tempDir.getAbsolutePath() + File.separator + "fail_next.mmap";
        int fileSize = 1024;

        // 启动服务线程
        Thread serviceThread = new Thread(service);
        serviceThread.start();

        try {
            // 等待一小段时间让服务线程启动
            Thread.sleep(100);

            MappedFile result = service.enqueue(filePath, nextFilePath, fileSize);

            // 应该返回 null，因为没有足够的缓冲区
            assertNull(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            try {
                service.shutdown();
                serviceThread.interrupt();
            } catch (Exception e) {
                // Ignore shutdown exceptions
            }
        }
    }

    @Test
    void testRequestQueuePriority() {
        AllocateRequest smallRequest = new AllocateRequest("/path/small", 512);
        AllocateRequest largeRequest = new AllocateRequest("/path/large", 2048);
        AllocateRequest mediumRequest = new AllocateRequest("/path/medium", 1024);

        // 大文件应该优先处理（负数表示更高优先级）
        assertTrue(largeRequest.compareTo(mediumRequest) < 0);
        assertTrue(mediumRequest.compareTo(smallRequest) < 0);
        assertTrue(largeRequest.compareTo(smallRequest) < 0);
    }

    @Test
    void testRequestEquality() {
        AllocateRequest request1 = new AllocateRequest("/path/test", 1024);
        AllocateRequest request2 = new AllocateRequest("/path/test", 1024);
        AllocateRequest request3 = new AllocateRequest("/path/different", 1024);
        AllocateRequest request4 = new AllocateRequest("/path/test", 2048);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());

        assertNotEquals(request1, request3);
        assertNotEquals(request1, request4);
    }

    @Test
    void testServiceName() {
        assertEquals("AllocateMappedFileService", service.getServiceName());
    }

}
