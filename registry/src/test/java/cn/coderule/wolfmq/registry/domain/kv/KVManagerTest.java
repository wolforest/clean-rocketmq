package cn.coderule.wolfmq.registry.domain.kv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KVManagerTest {

    @Test
    void testLifecycleDoesNotThrow() {
        KVManager manager = new KVManager();
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}