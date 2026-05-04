package cn.coderule.wolfmq.domain.core.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermNameTest {

    @Test
    void perm2String_zero() {
        assertEquals("---", PermName.perm2String(0));
    }

    @Test
    void perm2String_readAndWrite() {
        assertEquals("RW-", PermName.perm2String(PermName.PERM_READ | PermName.PERM_WRITE));
    }

    @Test
    void perm2String_readWriteInherit() {
        assertEquals("RWX", PermName.perm2String(PermName.PERM_READ | PermName.PERM_WRITE | PermName.PERM_INHERIT));
    }

    @Test
    void perm2String_readOnly() {
        assertEquals("R--", PermName.perm2String(PermName.PERM_READ));
    }

    @Test
    void perm2String_writeOnly() {
        assertEquals("-W-", PermName.perm2String(PermName.PERM_WRITE));
    }

    @Test
    void perm2String_inheritOnly() {
        assertEquals("--X", PermName.perm2String(PermName.PERM_INHERIT));
    }

    @Test
    void perm2String_writeAndInherit() {
        assertEquals("-WX", PermName.perm2String(PermName.PERM_WRITE | PermName.PERM_INHERIT));
    }

    @Test
    void perm2String_readAndInherit() {
        assertEquals("R-X", PermName.perm2String(PermName.PERM_READ | PermName.PERM_INHERIT));
    }

    @Test
    void isReadable_true() {
        assertTrue(PermName.isReadable(PermName.PERM_READ));
        assertTrue(PermName.isReadable(PermName.PERM_READ | PermName.PERM_WRITE));
    }

    @Test
    void isReadable_false() {
        assertFalse(PermName.isReadable(0));
        assertFalse(PermName.isReadable(PermName.PERM_WRITE));
    }

    @Test
    void isWriteable_true() {
        assertTrue(PermName.isWriteable(PermName.PERM_WRITE));
        assertTrue(PermName.isWriteable(PermName.PERM_READ | PermName.PERM_WRITE));
    }

    @Test
    void isWriteable_false() {
        assertFalse(PermName.isWriteable(0));
        assertFalse(PermName.isWriteable(PermName.PERM_READ));
    }

    @Test
    void isInherited_true() {
        assertTrue(PermName.isInherited(PermName.PERM_INHERIT));
        assertTrue(PermName.isInherited(PermName.PERM_READ | PermName.PERM_INHERIT));
    }

    @Test
    void isInherited_false() {
        assertFalse(PermName.isInherited(0));
        assertFalse(PermName.isInherited(PermName.PERM_READ));
    }

    @Test
    void isPriority_true() {
        assertTrue(PermName.isPriority(PermName.PERM_PRIORITY));
        assertTrue(PermName.isPriority(PermName.PERM_PRIORITY | PermName.PERM_READ));
    }

    @Test
    void isPriority_false() {
        assertFalse(PermName.isPriority(0));
        assertFalse(PermName.isPriority(PermName.PERM_READ));
    }

    @Test
    void isValid_int_validRange() {
        for (int i = 0; i < 8; i++) {
            assertTrue(PermName.isValid(i), "perm=" + i + " should be valid");
        }
    }

    @Test
    void isValid_int_invalidTooLarge() {
        assertFalse(PermName.isValid(8));
        assertFalse(PermName.isValid(15));
    }

    @Test
    void isValid_int_invalidNegative() {
        assertFalse(PermName.isValid(-1));
    }

    @Test
    void isValid_string_valid() {
        assertTrue(PermName.isValid("0"));
        assertTrue(PermName.isValid("6"));
        assertTrue(PermName.isValid("7"));
    }

    @Test
    void isValid_string_invalidTooLarge() {
        assertFalse(PermName.isValid("8"));
    }

    @Test
    void isValid_string_invalidNegative() {
        assertFalse(PermName.isValid("-1"));
    }
}