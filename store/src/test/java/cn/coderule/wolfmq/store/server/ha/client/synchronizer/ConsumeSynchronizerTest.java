package cn.coderule.wolfmq.store.server.ha.client.synchronizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumeSynchronizerTest {

    @Test
    void testImplementsSynchronizer() {
        ConsumeSynchronizer synchronizer = new ConsumeSynchronizer();
        assertInstanceOf(Synchronizer.class, synchronizer);
    }

    @Test
    void testSyncDoesNotThrow() {
        ConsumeSynchronizer synchronizer = new ConsumeSynchronizer();
        assertDoesNotThrow(synchronizer::sync);
    }
}
