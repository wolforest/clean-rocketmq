package cn.coderule.wolfmq.store.domain.commitlog.it;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.api.CommitLogStore;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.api.CommitLogStoreImpl;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import cn.coderule.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiShardCommitLogIntegrationTest {

    private static final int SHARD_COUNT = 3;
    private static final int MAX_SHARD = 3;
    private static final int MMAP_FILE_SIZE = 1024 * 1024;

    @TempDir
    Path tmpDir;

    private StoreConfig storeConfig;
    private AllocateMappedFileService allocateService;
    private CommitLogManager commitLogManager;
    private CommitLogStore commitLogStore;
    private TopicPartitioner partitioner;
    private OffsetCodec offsetCodec;

    @BeforeEach
    void setUp() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommitConfig().setFileSize(MMAP_FILE_SIZE);
        storeConfig.getCommitConfig().setShardingNumber(SHARD_COUNT);
        storeConfig.getCommitConfig().setMaxShardingNumber(MAX_SHARD);

        StoreContext.register(storeConfig);
        StoreContext.register(storeConfig.getCommitConfig());

        StoreCheckpoint checkpoint = new StoreCheckpoint(tmpDir.resolve("checkpoint").toString());
        checkpoint.setShutdownSuccessful(false);
        StoreContext.setCheckPoint(checkpoint);

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        StoreContext.register(allocateService);

        initCommitLog();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (commitLogManager != null) {
            commitLogManager.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        for (int shardId = 0; shardId < SHARD_COUNT; shardId++) {
            try {
                CommitLog cl = commitLogManager.selectByShardId(shardId);
                if (cl != null) {
                    cl.destroy();
                }
            } catch (Exception ignore) {
            }
        }
        StoreContext.CHECK_POINT = null;
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
    }

    private void resetStoreContext() {
        StoreContext.CHECK_POINT = null;
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
    }

    private void initCommitLog() throws Exception {
        CheckPoint ckpt = StoreContext.getCheckPoint();
        CommitConfig commitConfig = storeConfig.getCommitConfig();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, ckpt);
        List<CommitLog> logList = factory.createByConfig();

        partitioner = new TopicPartitioner(commitConfig);
        commitLogManager = new CommitLogManager(commitConfig, partitioner);
        commitLogManager.addCommitLog(logList);

        StoreContext.register(partitioner);
        StoreContext.register(commitLogManager);

        offsetCodec = new OffsetCodec(MAX_SHARD);

        commitLogStore = new CommitLogStoreImpl(commitLogManager);

        commitLogManager.initialize();
        commitLogManager.start();

        for (int i = 0; i < SHARD_COUNT; i++) {
            CommitLog cl = commitLogManager.selectByShardId(i);
            cl.getMappedFileQueue().getOrCreateMappedFileForSize(100);
        }
    }

    @Test
    void testOffsetCodecShardRouting() {
        assertEquals(0, offsetCodec.getShardId(0));
        assertEquals(1, offsetCodec.getShardId(1));
        assertEquals(2, offsetCodec.getShardId(2));
        assertEquals(0, offsetCodec.getShardId(MAX_SHARD));
        assertEquals(1, offsetCodec.getShardId(MAX_SHARD + 1));
    }

    @Test
    void testMultiShardInsertAndReadPerShard() {
        byte[] data0 = "shard0-AAAA".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "shard2-CCCC".getBytes(StandardCharsets.UTF_8);

        InsertResult r0 = commitLogStore.insert(0, data0, 0, data0.length);
        assertTrue(r0.isSuccess(), "insert to shard 0 should succeed");
        assertEquals(0, offsetCodec.getShardId(r0.getWroteOffset()));

        InsertResult r2 = commitLogStore.insert(2, data2, 0, data2.length);
        assertTrue(r2.isSuccess(), "insert to shard 2 should succeed");

        SelectedMappedBuffer buf0 = commitLogStore.select(r0.getWroteOffset());
        assertNotNull(buf0, "should read data from shard 0");
        byte[] read0 = new byte[data0.length];
        buf0.getByteBuffer().get(read0);
        assertArrayEquals(data0, read0, "data from shard 0 should match");
        buf0.release();
    }

    @Test
    void testMultiShardMinMaxOffset() {
        for (int shardId = 0; shardId < SHARD_COUNT; shardId++) {
            assertEquals(0, commitLogStore.getMinOffset(shardId),
                "minOffset for shard " + shardId + " should start at 0");
            assertEquals(0, commitLogStore.getMaxOffset(shardId),
                "maxOffset for shard " + shardId + " should start at 0");
        }

        byte[] data = "shard-offset-test".getBytes(StandardCharsets.UTF_8);
        commitLogStore.insert(0, data, 0, data.length);

        long maxOffsetShard0 = commitLogStore.getMaxOffset(0);
        assertTrue(maxOffsetShard0 > 0, "shard 0 maxOffset should increase after insert");

        for (int shardId = 1; shardId < SHARD_COUNT; shardId++) {
            assertEquals(0, commitLogStore.getMaxOffset(shardId),
                "shard " + shardId + " should still have maxOffset=0");
        }
    }
}