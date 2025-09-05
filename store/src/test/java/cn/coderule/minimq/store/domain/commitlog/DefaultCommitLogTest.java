package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.utils.test.ConfigMock;
import cn.coderule.minimq.store.domain.commitlog.flush.SyncCommitLogFlusher;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultCommitLogTest {
    public static int MMAP_FILE_SIZE = 2 * 1024 * 1024;

    @Test
    void testInsertAndSelect(@TempDir Path tmpDir) {
        CommitLog commitLog = createCommitLog(tmpDir.toString());
    }

    private CommitLog createCommitLog(String dir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setFileSize(MMAP_FILE_SIZE);

        MappedFileQueue queue = new DefaultMappedFileQueue(dir, MMAP_FILE_SIZE);
        CommitLogFlusher flusher = new SyncCommitLogFlusher(queue);

        return new DefaultCommitLog(storeConfig, queue, flusher);
    }

    private MessageBO createMessageBO() {
        return null;
    }
}
