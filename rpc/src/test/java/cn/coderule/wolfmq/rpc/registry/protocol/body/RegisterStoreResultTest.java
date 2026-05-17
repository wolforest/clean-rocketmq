package cn.coderule.wolfmq.rpc.registry.protocol.body;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterStoreResultTest {

    @Test
    void testSetters() {
        RegisterStoreResult result = new RegisterStoreResult();
        result.setHaServerAddr("haAddr");
        result.setMasterAddr("masterAddr");

        KVTable kvTable = new KVTable();
        result.setKvTable(kvTable);

        assertEquals("haAddr", result.getHaServerAddr());
        assertEquals("masterAddr", result.getMasterAddr());
        assertEquals(kvTable, result.getKvTable());
    }
}