package com.wolfmq.store.domain.index;

import com.wolfmq.common.domain.index.IndexService;
import com.wolfmq.common.domain.index.QueryOffsetResult;
import com.wolfmq.store.domain.index.file.FileIndexService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FileIndexServiceTest {

    @TempDir
    File tempDir;

    private FileIndexService createService() {
        return new FileIndexService(100, 1000, tempDir.getAbsolutePath());
    }

    @Test
    void testImplementsIndexService() {
        FileIndexService service = createService();
        assertInstanceOf(IndexService.class, service);
    }

    @Test
    void testConstructor() {
        FileIndexService service = createService();
        assertNotNull(service);
    }

    @Test
    void testBuildIndexWithNullKeysDoesNotThrow() {
        FileIndexService service = createService();
        assertDoesNotThrow(() -> service.buildIndex("topic", null, 0, System.currentTimeMillis()));
    }

    @Test
    void testBuildIndexWithEmptyKeysDoesNotThrow() {
        FileIndexService service = createService();
        assertDoesNotThrow(() -> service.buildIndex("topic", "", 0, System.currentTimeMillis()));
    }

    @Test
    void testBuildIndexCreatesIndexFile() {
        FileIndexService service = createService();
        long timestamp = System.currentTimeMillis();
        service.buildIndex("testTopic", "key1", 100L, timestamp);
    }

    @Test
    void testQueryOffsetWithEmptyKeyReturnsEmptyResult() {
        FileIndexService service = createService();
        QueryOffsetResult result = service.queryOffset("topic", "", 10, 0, 0);
        assertNotNull(result);
        assertTrue(result.getPhyOffsets().isEmpty());
    }

    @Test
    void testQueryOffsetWithNullKeyReturnsEmptyResult() {
        FileIndexService service = createService();
        QueryOffsetResult result = service.queryOffset("topic", null, 10, 0, 0);
        assertNotNull(result);
        assertTrue(result.getPhyOffsets().isEmpty());
    }

    @Test
    void testQueryOffsetReturnsResultWithMetadata() {
        FileIndexService service = createService();
        long timestamp = System.currentTimeMillis();
        service.buildIndex("testTopic", "key1", 100L, timestamp);

        QueryOffsetResult result = service.queryOffset("testTopic", "key1", 10, 0, 0);
        assertNotNull(result);
        assertFalse(result.getPhyOffsets().isEmpty());
        assertTrue(result.getIndexLastUpdateTimestamp() > 0);
    }

    @Test
    void testStartAndShutdownDoNotThrow() {
        FileIndexService service = createService();
        assertDoesNotThrow(service::start);
        assertDoesNotThrow(service::shutdown);
    }

    @Test
    void testLoadReturnsTrue() {
        FileIndexService service = createService();
        assertTrue(service.load(true));
    }

    @Test
    void testGetTotalSize() {
        FileIndexService service = createService();
        long timestamp = System.currentTimeMillis();
        service.buildIndex("testTopic", "key1", 100L, timestamp);
        assertTrue(service.getTotalSize() > 0);
    }

    @Test
    void testDestroy() {
        FileIndexService service = createService();
        assertDoesNotThrow(service::destroy);
    }

    @Test
    void testDeleteExpiredFile() {
        FileIndexService service = createService();
        assertDoesNotThrow(() -> service.deleteExpiredFile(0));
    }
}
