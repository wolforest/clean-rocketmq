package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.common.util.io.DirUtil;
import cn.coderule.common.util.io.FileUtil;
import cn.coderule.wolfmq.domain.domain.store.server.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StoreCheckpointTest {

    @TempDir
    Path tempDir;

    private StoreCheckpoint storeCheckpoint;

    @BeforeEach
    void setUp() {
        storeCheckpoint = new StoreCheckpoint(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        DirUtil.delete(tempDir.toString());
    }

    @Test
    void testConstructor() {
        assertNotNull(storeCheckpoint);
        assertFalse(storeCheckpoint.isShutdownSuccessful());
    }

    @Test
    void testLoadWithNoFiles() {
        storeCheckpoint.load();
        
        assertNotNull(storeCheckpoint.getMinOffset());
        assertNotNull(storeCheckpoint.getMaxOffset());
    }

    @Test
    void testGetMinOffset() {
        Offset minOffset = storeCheckpoint.getMinOffset();
        
        assertNotNull(minOffset);
        assertEquals(-1, minOffset.getCommitLogOffset());
    }

    @Test
    void testGetMaxOffset() {
        Offset maxOffset = storeCheckpoint.getMaxOffset();
        
        assertNotNull(maxOffset);
        assertEquals(-1, maxOffset.getCommitLogOffset());
    }

    @Test
    void testTryMinOffsetCreatesFile() {
        Offset minOffset = storeCheckpoint.tryMinOffset();
        
        assertNotNull(minOffset);
        String tryPath = tempDir.toString() + "/minOffset.json.try";
        assertTrue(FileUtil.exists(tryPath));
    }

    @Test
    void testTryMinOffsetTwiceReturnsSame() {
        Offset first = storeCheckpoint.tryMinOffset();
        Offset second = storeCheckpoint.tryMinOffset();
        
        assertEquals(first, second);
    }

    @Test
    void testCommitMinOffsetWithNoFiles() {
        storeCheckpoint.commitMinOffset();
        
        assertNotNull(storeCheckpoint.getMinOffset());
    }

    @Test
    void testCancelMinOffset() {
        storeCheckpoint.tryMinOffset();
        storeCheckpoint.cancelMinOffset();
        
        String tryPath = tempDir.toString() + "/minOffset.json.try";
        String commitPath = tempDir.toString() + "/minOffset.json.commit";
        assertFalse(FileUtil.exists(tryPath));
        assertFalse(FileUtil.exists(commitPath));
    }

    @Test
    void testSaveAndLoadMaxOffset() {
        Offset maxOffset = storeCheckpoint.getMaxOffset();
        maxOffset.setCommitLogOffset(100L);
        maxOffset.setDispatchedOffset(200L);
        maxOffset.setIndexOffset(300L);
        
        storeCheckpoint.saveMaxOffset();
        
        StoreCheckpoint newCheckpoint = new StoreCheckpoint(tempDir.toString());
        newCheckpoint.load();
        
        Offset loaded = newCheckpoint.getMaxOffset();
        assertEquals(100L, loaded.getCommitLogOffset());
        assertEquals(200L, loaded.getDispatchedOffset());
        assertEquals(300L, loaded.getIndexOffset());
    }

    @Test
    void testShutdownSuccessful() {
        assertFalse(storeCheckpoint.isShutdownSuccessful());
        
        storeCheckpoint.setShutdownSuccessful(true);
        
        assertTrue(storeCheckpoint.isShutdownSuccessful());
    }
}
