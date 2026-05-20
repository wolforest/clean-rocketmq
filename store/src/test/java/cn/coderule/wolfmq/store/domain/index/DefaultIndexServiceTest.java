package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.domain.store.domain.index.IndexService;
import cn.coderule.wolfmq.domain.domain.store.domain.index.QueryOffsetResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultIndexServiceTest {

    @TempDir
    File tempDir;

    private DefaultIndexService createService() {
        return new DefaultIndexService(100, 1000, tempDir.getAbsolutePath());
    }

    @Test
    void testImplementsIndexService() {
        DefaultIndexService service = createService();
        assertInstanceOf(IndexService.class, service);
    }

    @Test
    void testConstructor() {
        DefaultIndexService service = createService();
        assertNotNull(service);
    }

    @Test
    void testBuildIndexWithNullKeysDoesNotThrow() {
        DefaultIndexService service = createService();
        assertDoesNotThrow(() -> service.buildIndex("topic", null, 0, System.currentTimeMillis()));
    }

    @Test
    void testBuildIndexWithEmptyKeysDoesNotThrow() {
        DefaultIndexService service = createService();
        assertDoesNotThrow(() -> service.buildIndex("topic", "", 0, System.currentTimeMillis()));
    }

    @Test
    void testBuildIndexCreatesIndexFile() {
        DefaultIndexService service = createService();
        long timestamp = System.currentTimeMillis();
        service.buildIndex("testTopic", "key1", 100L, timestamp);
    }

    @Test
    void testQueryOffsetWithEmptyKeyReturnsEmptyResult() {
        DefaultIndexService service = createService();
        QueryOffsetResult result = service.queryOffset("topic", "", 10, 0, 0);
        assertNotNull(result);
        assertTrue(result.getPhyOffsets().isEmpty());
    }

    @Test
    void testQueryOffsetWithNullKeyReturnsEmptyResult() {
        DefaultIndexService service = createService();
        QueryOffsetResult result = service.queryOffset("topic", null, 10, 0, 0);
        assertNotNull(result);
        assertTrue(result.getPhyOffsets().isEmpty());
    }

    @Test
    void testQueryOffsetReturnsResultWithMetadata() {
        DefaultIndexService service = createService();
        long timestamp = System.currentTimeMillis();
        service.buildIndex("testTopic", "key1", 100L, timestamp);

        QueryOffsetResult result = service.queryOffset("testTopic", "key1", 10, 0, 0);
        assertNotNull(result);
        assertFalse(result.getPhyOffsets().isEmpty());
        assertTrue(result.getIndexLastUpdateTimestamp() > 0);
    }

    @Test
    void testStartAndShutdownDoNotThrow() {
        DefaultIndexService service = createService();
        assertDoesNotThrow(service::start);
        assertDoesNotThrow(service::shutdown);
    }

    @Test
    void testLoadReturnsTrue() {
        DefaultIndexService service = createService();
        assertTrue(service.load(true));
    }

    @Test
    void testGetTotalSize() {
        DefaultIndexService service = createService();
        long timestamp = System.currentTimeMillis();
        service.buildIndex("testTopic", "key1", 100L, timestamp);
        assertTrue(service.getTotalSize() > 0);
    }

    @Test
    void testDestroy() {
        DefaultIndexService service = createService();
        assertDoesNotThrow(service::destroy);
    }

    @Test
    void testDeleteExpiredFile() {
        DefaultIndexService service = createService();
        assertDoesNotThrow(() -> service.deleteExpiredFile(0));
    }
}