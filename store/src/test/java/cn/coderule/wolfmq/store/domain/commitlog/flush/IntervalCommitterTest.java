package cn.coderule.wolfmq.store.domain.commitlog.flush;

import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
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
