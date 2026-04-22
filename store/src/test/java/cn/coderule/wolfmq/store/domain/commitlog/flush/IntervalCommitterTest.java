package cn.coderule.wolfmq.store.domain.commitlog.flush;

import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IntervalCommitterTest {

    private CommitConfig config;
    private MappedFileQueue mappedFileQueue;
    private Flusher flusher;
    private IntervalCommitter committer;

    @BeforeEach
    void setUp() {
        config = new CommitConfig();
        mappedFileQueue = mock(MappedFileQueue.class);
        flusher = mock(Flusher.class);
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