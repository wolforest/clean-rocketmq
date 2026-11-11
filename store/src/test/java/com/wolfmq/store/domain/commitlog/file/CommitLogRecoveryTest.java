package com.wolfmq.store.domain.commitlog.file;

import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitLogRecoveryTest {

    private FileCommitLog commitLog;
    private QueueProgress queueProgress;
    private CommitLogRecovery recovery;

    @BeforeEach
    void setUp() {
        commitLog = mock(FileCommitLog.class);
        queueProgress = mock(QueueProgress.class);
        recovery = new CommitLogRecovery(commitLog, queueProgress);
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
        when(queueProgress.getCommitOffset(commitLog.getShardId())).thenReturn(null);

        assertDoesNotThrow(() -> recovery.recover());
    }

    @Test
    void testRecoverWithMaxOffset() {
        MappedFileQueue mfq = mock(MappedFileQueue.class);
        when(commitLog.getMappedFileQueue()).thenReturn(mfq);
        when(mfq.isEmpty()).thenReturn(false);


        when(queueProgress.getCommitOffset(commitLog.getShardId())).thenReturn(100L);
        when(queueProgress.isShutdownSuccessfully()).thenReturn(true);
        when(commitLog.getOffsetCodec()).thenReturn(new OffsetCodec(1));

        assertDoesNotThrow(() -> recovery.recover());
    }
}
