package com.wolfmq.store.domain.commitlog.flush;

import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
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
