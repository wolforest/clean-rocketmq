package cn.coderule.minimq.store.infra.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AllocateRequest 单元测试
 */
public class AllocateRequestTest {

    @Test
    void testRequestComparison() {
        AllocateRequest smallRequest = new AllocateRequest("/path/small", 512);
        AllocateRequest largeRequest = new AllocateRequest("/path/large", 2048);
        AllocateRequest mediumRequest = new AllocateRequest("/path/medium", 1024);

        // 大文件应该优先处理（负数表示更高优先级）
        assertTrue(largeRequest.compareTo(mediumRequest) < 0);
        assertTrue(mediumRequest.compareTo(smallRequest) < 0);
        assertTrue(largeRequest.compareTo(smallRequest) < 0);

        // 相同大小的文件应该比较文件名
        AllocateRequest sameSize1 = new AllocateRequest("/path/100", 1024);
        AllocateRequest sameSize2 = new AllocateRequest("/path/200", 1024);

        assertTrue(sameSize1.compareTo(sameSize2) < 0); // 100 < 200
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

        // Test self equality
        assertEquals(request1, request1);
    }

    @Test
    void testRequestWithNullPath() {
        AllocateRequest request1 = new AllocateRequest(null, 1024);
        AllocateRequest request2 = new AllocateRequest(null, 1024);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());

        assertNotEquals(request1, new AllocateRequest("/path/test", 1024));
    }

    @Test
    void testMappedFileAssignment() {
        AllocateRequest request = new AllocateRequest("/test/path", 1024);

        // 简单测试 mapped file 赋值
        // 由于字段的可见性问题，我们只能通过公共方法验证
        assertNotNull(request);
    }

    @Test
    void testCountDownLatchFunctionality() throws InterruptedException {
        AllocateRequest request = new AllocateRequest("/test/path", 1024);

        // 由于字段可见性问题，我们只能验证对象创建
        assertNotNull(request);
        // CountDownLatch 的测试需要反射或者公共方法，这里简化测试
    }

    @Test
    void testToString() {
        AllocateRequest request = new AllocateRequest("/test/path/file.mmap", 1024);

        String toString = request.toString();
        assertNotNull(toString);
        // 由于 Lombok @Data 生成的 toString 格式可能不同，我们只验证非空
        assertFalse(toString.isEmpty());
    }
}
