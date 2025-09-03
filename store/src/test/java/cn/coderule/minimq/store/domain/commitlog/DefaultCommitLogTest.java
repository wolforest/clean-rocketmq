package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.utils.test.ConfigTest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCommitLogTest {
    public static int MMAP_FILE_SIZE = 2 * 1024 * 1024;

    @Test
    void testInsertAndSelect(@TempDir Path tmpDir) {
        CommitLog commitLog = createCommitLog(tmpDir.toString());
    }

    private CommitLog createCommitLog(String dir) {
        StoreConfig storeConfig = ConfigTest.createStoreConfig(dir);
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setFileSize(MMAP_FILE_SIZE);

        return null;
    }

    private MessageBO createMessageBO() {
        return null;
    }
}
