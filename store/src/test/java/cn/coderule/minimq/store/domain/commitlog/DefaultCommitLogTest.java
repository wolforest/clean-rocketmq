package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCommitLogTest {

    @Test
    void testInsertAndSelect(@TempDir Path tmpDir) {
        CommitLog commitLog = createCommitLog(tmpDir);
    }

    private CommitLog createCommitLog(Path tmpDir) {
        return null;
    }

    private MessageBO createMessageBO() {
        return null;
    }
}
