package com.wolfmq.registry.domain.property;

import cn.coderule.common.convention.container.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyManagerTest {

    @Test
    void testLifecycleDoesNotThrow() {
        PropertyManager manager = new PropertyManager(new ApplicationContext());
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}