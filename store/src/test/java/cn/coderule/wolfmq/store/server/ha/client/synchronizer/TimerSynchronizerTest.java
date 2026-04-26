package cn.coderule.wolfmq.store.server.ha.client.synchronizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TimerSynchronizerTest {

    @Test
    void testImplementsSynchronizer() {
        TimerSynchronizer synchronizer = new TimerSynchronizer();
        assertInstanceOf(Synchronizer.class, synchronizer);
    }

    @Test
    void testSyncDoesNotThrow() {
        TimerSynchronizer synchronizer = new TimerSynchronizer();
        assertDoesNotThrow(synchronizer::sync);
    }
}
