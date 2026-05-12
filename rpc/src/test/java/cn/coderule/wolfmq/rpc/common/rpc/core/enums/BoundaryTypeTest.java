package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundaryTypeTest {

    @Test
    void testEnumValues() {
        BoundaryType[] values = BoundaryType.values();
        assertTrue(values.length > 0);
    }

    @Test
    void testValueOf() {
        for (BoundaryType type : BoundaryType.values()) {
            assertEquals(type, BoundaryType.valueOf(type.name()));
        }
    }
}