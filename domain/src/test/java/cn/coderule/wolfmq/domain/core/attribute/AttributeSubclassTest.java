package cn.coderule.wolfmq.domain.core.attribute;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AttributeSubclassTest {

    // --- BooleanAttribute tests ---

    @Test
    void booleanAttribute_verify_true() {
        BooleanAttribute attr = new BooleanAttribute("test", true, false);
        assertDoesNotThrow(() -> attr.verify("true"));
    }

    @Test
    void booleanAttribute_verify_false() {
        BooleanAttribute attr = new BooleanAttribute("test", true, false);
        assertDoesNotThrow(() -> attr.verify("false"));
    }

    @Test
    void booleanAttribute_verify_caseInsensitive() {
        BooleanAttribute attr = new BooleanAttribute("test", true, false);
        assertDoesNotThrow(() -> attr.verify("TRUE"));
        assertDoesNotThrow(() -> attr.verify("FALSE"));
    }

    @Test
    void booleanAttribute_verify_invalidValue_throws() {
        BooleanAttribute attr = new BooleanAttribute("test", true, false);
        assertThrows(RuntimeException.class, () -> attr.verify("maybe"));
    }

    @Test
    void booleanAttribute_verify_null_throws() {
        BooleanAttribute attr = new BooleanAttribute("test", true, false);
        assertThrows(NullPointerException.class, () -> attr.verify(null));
    }

    @Test
    void booleanAttribute_getDefaultValue() {
        BooleanAttribute attr = new BooleanAttribute("test", true, true);
        assertTrue(attr.getDefaultValue());

        BooleanAttribute attr2 = new BooleanAttribute("test", true, false);
        assertFalse(attr2.getDefaultValue());
    }

    // --- LongRangeAttribute tests ---

    @Test
    void longRangeAttribute_verify_inRange() {
        LongRangeAttribute attr = new LongRangeAttribute("test", true, 1, 100, 50);
        assertDoesNotThrow(() -> attr.verify("1"));
        assertDoesNotThrow(() -> attr.verify("50"));
        assertDoesNotThrow(() -> attr.verify("100"));
    }

    @Test
    void longRangeAttribute_verify_belowMin_throws() {
        LongRangeAttribute attr = new LongRangeAttribute("test", true, 1, 100, 50);
        assertThrows(RuntimeException.class, () -> attr.verify("0"));
    }

    @Test
    void longRangeAttribute_verify_aboveMax_throws() {
        LongRangeAttribute attr = new LongRangeAttribute("test", true, 1, 100, 50);
        assertThrows(RuntimeException.class, () -> attr.verify("101"));
    }

    @Test
    void longRangeAttribute_verify_nonNumeric_throws() {
        LongRangeAttribute attr = new LongRangeAttribute("test", true, 1, 100, 50);
        assertThrows(NumberFormatException.class, () -> attr.verify("abc"));
    }

    @Test
    void longRangeAttribute_getDefaultValue() {
        LongRangeAttribute attr = new LongRangeAttribute("test", true, 1, 100, 50);
        assertEquals(50, attr.getDefaultValue());
    }

    // --- EnumAttribute tests ---

    @Test
    void enumAttribute_verify_validValue() {
        EnumAttribute attr = new EnumAttribute("test", true, Set.of("low", "medium", "high"), "medium");
        assertDoesNotThrow(() -> attr.verify("low"));
        assertDoesNotThrow(() -> attr.verify("medium"));
        assertDoesNotThrow(() -> attr.verify("high"));
    }

    @Test
    void enumAttribute_verify_invalidValue_throws() {
        EnumAttribute attr = new EnumAttribute("test", true, Set.of("low", "medium", "high"), "medium");
        assertThrows(RuntimeException.class, () -> attr.verify("invalid"));
    }

    @Test
    void enumAttribute_getDefaultValue() {
        EnumAttribute attr = new EnumAttribute("test", true, Set.of("low", "medium", "high"), "medium");
        assertEquals("medium", attr.getDefaultValue());
    }
}