package cn.coderule.wolfmq.store.domain.meta;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultConsumeOffsetServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void load_WhenFileMissing_ShouldInitializeAndSupportPutGet() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        String storePath = tempDir.resolve("consume-offset.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);

        service.load();
        service.putOffset("g1", "t1", 0, 123L);

        assertEquals(123L, service.getOffset("g1", "t1", 0));
        assertTrue(service.findTopicByGroup("g1").contains("t1"));
        assertTrue(service.findGroupByTopic("t1").contains("g1"));
    }

    @Test
    void storeAndLoad_ShouldPersistOffsets() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        String storePath = tempDir.resolve("consume-offset.json").toString();

        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();
        service.putOffset("g1", "t1", 1, 456L);
        service.store();

        DefaultConsumeOffsetService reloaded = new DefaultConsumeOffsetService(storeConfig, storePath);
        reloaded.load();

        assertEquals(456L, reloaded.getOffset("g1", "t1", 1));

        reloaded.deleteByTopic("t1");
        assertEquals(-1L, reloaded.getOffset("g1", "t1", 1));
    }
}

