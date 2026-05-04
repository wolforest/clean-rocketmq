package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TlsModeTest {

    @Test
    void parseDisabledReturnsDisabled() {
        assertEquals(TlsMode.DISABLED, TlsMode.parse("disabled"));
    }

    @Test
    void parsePermissiveReturnsPermissive() {
        assertEquals(TlsMode.PERMISSIVE, TlsMode.parse("permissive"));
    }

    @Test
    void parseEnforcingReturnsEnforcing() {
        assertEquals(TlsMode.ENFORCING, TlsMode.parse("enforcing"));
    }

    @Test
    void parseNullReturnsPermissiveDefault() {
        assertEquals(TlsMode.PERMISSIVE, TlsMode.parse(null));
    }

    @Test
    void parseUnknownReturnsPermissiveDefault() {
        assertEquals(TlsMode.PERMISSIVE, TlsMode.parse("unknown"));
    }

    @Test
    void parseEmptyReturnsPermissiveDefault() {
        assertEquals(TlsMode.PERMISSIVE, TlsMode.parse(""));
    }

    @Test
    void getNameReturnsCorrectString() {
        assertEquals("disabled", TlsMode.DISABLED.getName());
        assertEquals("permissive", TlsMode.PERMISSIVE.getName());
        assertEquals("enforcing", TlsMode.ENFORCING.getName());
    }
}