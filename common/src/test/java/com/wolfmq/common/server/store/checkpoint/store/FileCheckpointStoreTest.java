package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.store.FileCheckpointStore;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class FileCheckpointStoreTest {

    @Test
    void saveAndLoadShouldRestoreOffsets(@TempDir Path tempDir) {
        FileCheckpointStore store = new FileCheckpointStore(tempDir.toString());
        store.updateCommitOffset(1, 100L);
        store.updateDispatchedOffset(2, 200L);
        store.updateMasterOffset(3, 300L);
        store.updateIndexedOffset(4, 400L);
        store.updateQueueOffset("topic", 1, 500L);
        store.updateQueueCommitOffset("topic", 1, 2, 600L);

        store.save();

        FileCheckpointStore loaded = new FileCheckpointStore(tempDir.toString());
        loaded.load();

        assertEquals(100L, loaded.getCommitOffset(1));
        assertEquals(200L, loaded.getDispatchedOffset(2));
        assertEquals(300L, loaded.getMasterOffset(3));
        assertEquals(400L, loaded.getIndexedOffset(4));
        assertEquals(500L, loaded.getQueueOffset("topic", 1));
        assertEquals(600L, loaded.getQueueCommitOffset("topic", 1, 2));
    }
}
