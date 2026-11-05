package com.wolfmq.common.server.store.checkpoint.store;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class FileCheckpointStoreTest {

    @Test
    void saveAndLoadShouldRestoreOffsets(@TempDir Path tempDir) {
        FileCheckpointStore store = new FileCheckpointStore(tempDir.toString());
        store.setCommitOffset(1, 100L);
        store.setDispatchedOffset(2, 200L);
        store.setMasterOffset(3, 300L);
        store.setIndexedOffset(4, 400L);
        store.setConsumeQueueOffset("topic", 1, 2, 500L);
        store.setQueuedOffset("topic", 1, 2, 600L);

        store.save();

        FileCheckpointStore loaded = new FileCheckpointStore(tempDir.toString());
        loaded.load();

        assertEquals(100L, loaded.getCommitOffset(1));
        assertEquals(200L, loaded.getDispatchedOffset(2));
        assertEquals(300L, loaded.getMasterOffset(3));
        assertEquals(400L, loaded.getIndexedOffset(4));
        assertEquals(500L, loaded.getConsumeQueueOffset("topic", 1, 2));
        assertEquals(600L, loaded.getQueuedOffset("topic", 1, 2));
    }
}
