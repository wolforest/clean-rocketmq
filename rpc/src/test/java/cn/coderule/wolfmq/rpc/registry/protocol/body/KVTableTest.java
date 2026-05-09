package cn.coderule.wolfmq.rpc.registry.protocol.body;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class KVTableTest {

    @Test
    void testDefaultConstructor() {
        KVTable kvTable = new KVTable();
        
        assertNotNull(kvTable);
        assertNotNull(kvTable.getTable());
        assertTrue(kvTable.getTable().isEmpty());
    }

    @Test
    void testSetAndGetTable() {
        KVTable kvTable = new KVTable();
        HashMap<String, String> table = new HashMap<>();
        table.put("key1", "value1");
        table.put("key2", "value2");
        
        kvTable.setTable(table);
        
        assertEquals(table, kvTable.getTable());
        assertEquals("value1", kvTable.getTable().get("key1"));
        assertEquals("value2", kvTable.getTable().get("key2"));
    }

    @Test
    void testInheritance() {
        KVTable kvTable = new KVTable();
        
        assertTrue(kvTable instanceof cn.coderule.wolfmq.rpc.common.rpc.protocol.codec.RpcSerializable);
    }

    @Test
    void testTableEquality() {
        KVTable kvTable1 = new KVTable();
        HashMap<String, String> table1 = new HashMap<>();
        table1.put("key", "value");
        kvTable1.setTable(table1);
        
        KVTable kvTable2 = new KVTable();
        HashMap<String, String> table2 = new HashMap<>();
        table2.put("key", "value");
        kvTable2.setTable(table2);
        
        assertEquals(kvTable1.getTable(), kvTable2.getTable());
    }
}
