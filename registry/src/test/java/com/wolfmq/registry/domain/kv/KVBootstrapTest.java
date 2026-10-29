package com.wolfmq.registry.domain.kv;

import cn.coderule.common.convention.container.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KVBootstrapTest {

    @Test
    void testLifecycleDoesNotThrow() {
        KVBootstrap manager = new KVBootstrap(new ApplicationContext());
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}
