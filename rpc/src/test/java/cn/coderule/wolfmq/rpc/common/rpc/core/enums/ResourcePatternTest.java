package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourcePatternTest {

    @Test
    void testEnumValues() {
        assertEquals(3, ResourcePattern.values().length);
    }

    @Test
    void testAnyPattern() {
        assertEquals((byte) 1, ResourcePattern.ANY.getCode());
        assertEquals("ANY", ResourcePattern.ANY.getName());
    }

    @Test
    void testLiteralPattern() {
        assertEquals((byte) 2, ResourcePattern.LITERAL.getCode());
        assertEquals("LITERAL", ResourcePattern.LITERAL.getName());
    }

    @Test
    void testPrefixedPattern() {
        assertEquals((byte) 3, ResourcePattern.PREFIXED.getCode());
        assertEquals("PREFIXED", ResourcePattern.PREFIXED.getName());
    }

    @Test
    void testValueOf() {
        assertEquals(ResourcePattern.ANY, ResourcePattern.valueOf("ANY"));
        assertEquals(ResourcePattern.LITERAL, ResourcePattern.valueOf("LITERAL"));
        assertEquals(ResourcePattern.PREFIXED, ResourcePattern.valueOf("PREFIXED"));
    }
}