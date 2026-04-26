package cn.coderule.wolfmq.store.server.ha.client;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.store.api.CommitLogStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SlaveOffsetReporterTest {

    private SlaveOffsetReporter reporter;

    @BeforeEach
    void setUp() {
        DefaultHAClient haClient = mock(DefaultHAClient.class);
        StoreConfig storeConfig = new StoreConfig();
        when(haClient.getStoreConfig()).thenReturn(storeConfig);
        CommitLogStore commitLogStore = mock(CommitLogStore.class);
        when(commitLogStore.getMaxOffset(0)).thenReturn(0L);
        when(haClient.getCommitLogStore()).thenReturn(commitLogStore);
        reporter = new SlaveOffsetReporter(haClient);
    }

    @Test
    void testHeartbeatReturnsTrueWhenNotTime() {
        assertTrue(reporter.heartbeat());
    }

    @Test
    void testReportReturnsTrueWhenCurrentOffsetLEReportedOffset() {
        assertTrue(reporter.report());
    }

    @Test
    void testStartDoesNotThrow() {
        assertDoesNotThrow(() -> reporter.start());
    }

    @Test
    void testShutdownDoesNotThrow() {
        assertDoesNotThrow(() -> reporter.shutdown());
    }

    @Test
    void testInitializeDoesNotThrow() {
        assertDoesNotThrow(() -> reporter.initialize());
    }
}
