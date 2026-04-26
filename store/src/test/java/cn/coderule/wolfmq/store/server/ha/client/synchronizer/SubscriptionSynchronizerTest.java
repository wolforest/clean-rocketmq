package cn.coderule.wolfmq.store.server.ha.client.synchronizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionSynchronizerTest {

    @Test
    void testImplementsSynchronizer() {
        SubscriptionSynchronizer synchronizer = new SubscriptionSynchronizer();
        assertInstanceOf(Synchronizer.class, synchronizer);
    }

    @Test
    void testSyncDoesNotThrow() {
        SubscriptionSynchronizer synchronizer = new SubscriptionSynchronizer();
        assertDoesNotThrow(synchronizer::sync);
    }
}
