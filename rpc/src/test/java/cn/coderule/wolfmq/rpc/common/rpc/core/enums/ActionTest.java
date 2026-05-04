package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActionTest {

    @Test
    void getByNameExactCase() {
        assertEquals(Action.PUB, Action.getByName("Pub"));
        assertEquals(Action.SUB, Action.getByName("Sub"));
        assertEquals(Action.ALL, Action.getByName("All"));
        assertEquals(Action.UNKNOWN, Action.getByName("Unknown"));
    }

    @Test
    void getByNameCaseInsensitive() {
        assertEquals(Action.PUB, Action.getByName("pub"));
        assertEquals(Action.PUB, Action.getByName("PUB"));
        assertEquals(Action.PUB, Action.getByName("pUb"));
    }

    @Test
    void getByNameNullReturnsNull() {
        assertNull(Action.getByName(null));
    }

    @Test
    void getByNameNonexistentReturnsNull() {
        assertNull(Action.getByName("nonexistent"));
    }

    @Test
    void getCodeReturnsCorrectByteValues() {
        assertEquals(0, Action.UNKNOWN.getCode());
        assertEquals(1, Action.ALL.getCode());
        assertEquals(2, Action.ANY.getCode());
        assertEquals(3, Action.PUB.getCode());
        assertEquals(4, Action.SUB.getCode());
        assertEquals(5, Action.CREATE.getCode());
        assertEquals(6, Action.UPDATE.getCode());
        assertEquals(7, Action.DELETE.getCode());
        assertEquals(8, Action.GET.getCode());
        assertEquals(9, Action.LIST.getCode());
    }

    @Test
    void valuesHasTenEntries() {
        assertEquals(10, Action.values().length);
    }
}