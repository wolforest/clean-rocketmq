package cn.coderule.wolfmq.store.server.ha.client.synchronizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TopicSynchronizerTest {

    @Test
    void testImplementsSynchronizer() {
        TopicSynchronizer synchronizer = new TopicSynchronizer();
        assertInstanceOf(Synchronizer.class, synchronizer);
    }

    @Test
    void testSyncDoesNotThrow() {
        TopicSynchronizer synchronizer = new TopicSynchronizer();
        assertDoesNotThrow(synchronizer::sync);
    }
}
