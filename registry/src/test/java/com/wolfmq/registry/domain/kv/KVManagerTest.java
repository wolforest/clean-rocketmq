package com.wolfmq.registry.domain.kv;

import cn.coderule.common.convention.container.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KVManagerTest {

    @Test
    void testLifecycleDoesNotThrow() {
        KVManager manager = new KVManager(new ApplicationContext());
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}