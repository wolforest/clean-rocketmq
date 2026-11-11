package com.wolfmq.store.domain.commitlog.flush;

import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.store.domain.commitlog.flush.flusher.Flusher;
import com.wolfmq.store.domain.commitlog.flush.flusher.IntervalCommitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IntervalCommitterTest {

    private IntervalCommitter committer;

    @BeforeEach
    void setUp() {
        CommitConfig config = new CommitConfig();
        MappedFileQueue mappedFileQueue = mock(MappedFileQueue.class);
        Flusher flusher = mock(Flusher.class);
        committer = new IntervalCommitter(config, mappedFileQueue, flusher);
    }

    @Test
    void testGetServiceName() {
        assertEquals("IntervalCommitter", committer.getServiceName());
    }

    @Test
    void testSetMaxOffset() {
        committer.setMaxOffset(1000L);
    }

    @Test
    void testSetMaxOffsetOnlyIncreases() {
        committer.setMaxOffset(1000L);
        committer.setMaxOffset(500L);
    }
}
