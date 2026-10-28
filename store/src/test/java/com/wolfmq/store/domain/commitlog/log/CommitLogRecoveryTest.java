package com.wolfmq.store.domain.commitlog.log;

import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.domain.server.store.Offset;
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
        when(commitLog.getMappedFileQueue()).thenReturn(mock(MappedFileQueue.class));
        when(commitLog.getMappedFileQueue().isEmpty()).thenReturn(true);

        assertDoesNotThrow(() -> recovery.recover());
    }

    @Test
    void testRecoverWithNonEmptyMappedFileQueue() {
        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(commitLog.getMappedFileQueue()).thenReturn(mfq);
        when(mfq.isEmpty()).thenReturn(false);
        when(checkPoint.getMaxOffset()).thenReturn(null);

        assertDoesNotThrow(() -> recovery.recover());
    }

    @Test
    void testRecoverWithMaxOffset() {
        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(commitLog.getMappedFileQueue()).thenReturn(mfq);
        when(mfq.isEmpty()).thenReturn(false);

        Offset offset = mock(Offset.class);
        when(offset.getCommitLogOffset()).thenReturn(100L);
        when(checkPoint.getMaxOffset()).thenReturn(offset);
        when(checkPoint.isShutdownSuccessful()).thenReturn(true);

        assertDoesNotThrow(() -> recovery.recover());
    }
}
