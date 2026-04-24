package cn.coderule.wolfmq.store.domain.commitlog.log;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitLogRecoveryTest {

    private CommitLog commitLog;
    private CheckPoint checkPoint;
    private CommitLogRecovery recovery;

    @BeforeEach
    void setUp() {
        commitLog = mock(CommitLog.class);
        checkPoint = mock(CheckPoint.class);
        recovery = new CommitLogRecovery(commitLog, checkPoint);
    }

    @Test
    void testRecoverWithEmptyMappedFileQueue() {
        when(commitLog.getMappedFileQueue()).thenReturn(mock(cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue.class));
        when(commitLog.getMappedFileQueue().isEmpty()).thenReturn(true);

        assertDoesNotThrow(() -> recovery.recover());
    }

    @Test
    void testRecoverWithNonEmptyMappedFileQueue() {
        cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue mfq = mock(cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue.class);
        when(commitLog.getMappedFileQueue()).thenReturn(mfq);
        when(mfq.isEmpty()).thenReturn(false);
        when(checkPoint.getMaxOffset()).thenReturn(null);

        assertDoesNotThrow(() -> recovery.recover());
    }

    @Test
    void testRecoverWithMaxOffset() {
        cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue mfq = mock(cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue.class);
        when(commitLog.getMappedFileQueue()).thenReturn(mfq);
        when(mfq.isEmpty()).thenReturn(false);

        cn.coderule.wolfmq.domain.domain.store.server.Offset offset = mock(cn.coderule.wolfmq.domain.domain.store.server.Offset.class);
        when(offset.getCommitLogOffset()).thenReturn(100L);
        when(checkPoint.getMaxOffset()).thenReturn(offset);
        when(checkPoint.isShutdownSuccessful()).thenReturn(true);

        assertDoesNotThrow(() -> recovery.recover());
    }
}