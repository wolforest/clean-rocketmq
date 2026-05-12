package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTypeTest {

    @Test
    void testEnumValues() {
        ResourceType[] values = ResourceType.values();
        assertTrue(values.length > 0);
    }

    @Test
    void testTopicResource() {
        assertNotNull(ResourceType.TOPIC);
    }

    @Test
    void testGroupResource() {
        assertNotNull(ResourceType.GROUP);
    }

    @Test
    void testValueOf() {
        assertNotNull(ResourceType.valueOf("TOPIC"));
    }
}