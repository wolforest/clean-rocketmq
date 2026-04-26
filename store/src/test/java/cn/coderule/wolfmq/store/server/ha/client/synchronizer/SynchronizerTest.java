package cn.coderule.wolfmq.store.server.ha.client.synchronizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SynchronizerTest {

    @Test
    void testInterfaceExists() {
        Synchronizer sync = () -> {};
        assertNotNull(sync);
    }

    @Test
    void testSyncMethodCalled() {
        boolean[] called = {false};
        Synchronizer sync = () -> called[0] = true;
        sync.sync();
        assertTrue(called[0]);
    }

    @Test
    void testImplementationsCanBeDifferent() {
        Synchronizer noop = () -> {};
        Synchronizer tracking = () -> {};
        assertNotNull(noop);
        assertNotNull(tracking);
    }
}