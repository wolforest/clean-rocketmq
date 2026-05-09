package cn.coderule.wolfmq.registry.domain.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyManagerTest {

    @Test
    void testLifecycleDoesNotThrow() {
        PropertyManager manager = new PropertyManager();
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}