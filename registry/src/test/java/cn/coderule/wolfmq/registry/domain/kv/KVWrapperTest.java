package cn.coderule.wolfmq.registry.domain.kv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class KVWrapperTest {

    @Test
    void testDefaultConstructor() {
        KVWrapper wrapper = new KVWrapper();
        
        assertNotNull(wrapper);
        assertNull(wrapper.getConfigTable());
    }

    @Test
    void testSetAndGetConfigTable() {
        KVWrapper wrapper = new KVWrapper();
        HashMap<String, HashMap<String, String>> configTable = new HashMap<>();
        
        HashMap<String, String> ns1 = new HashMap<>();
        ns1.put("key1", "value1");
        configTable.put("ns1", ns1);
        
        wrapper.setConfigTable(configTable);
        
        assertNotNull(wrapper.getConfigTable());
        assertEquals(1, wrapper.getConfigTable().size());
        assertEquals("value1", wrapper.getConfigTable().get("ns1").get("key1"));
    }

    @Test
    void testInheritance() {
        KVWrapper wrapper = new KVWrapper();
        
        assertTrue(wrapper instanceof cn.coderule.wolfmq.rpc.common.rpc.protocol.codec.RpcSerializable);
    }
}