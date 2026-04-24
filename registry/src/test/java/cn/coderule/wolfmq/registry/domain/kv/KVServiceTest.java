package cn.coderule.wolfmq.registry.domain.kv;

import cn.coderule.wolfmq.domain.config.server.RegistryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KVServiceTest {

    private KVService kvService;
    private RegistryConfig config;

    @BeforeEach
    void setUp() {
        config = mock(RegistryConfig.class);
        when(config.getKvPath()).thenReturn("/tmp/test-kv-config.json");
        kvService = new KVService(config);
    }

    @Test
    void testPutAndGetKVConfig() {
        kvService.putKVConfig("namespace1", "key1", "value1");
        
        String value = kvService.getKVConfig("namespace1", "key1");
        assertEquals("value1", value);
    }

    @Test
    void testPutKVConfigUpdatesValue() {
        kvService.putKVConfig("namespace1", "key1", "value1");
        kvService.putKVConfig("namespace1", "key1", "value2");
        
        String value = kvService.getKVConfig("namespace1", "key1");
        assertEquals("value2", value);
    }

    @Test
    void testGetKVConfigNotFound() {
        String value = kvService.getKVConfig("namespace1", "non-existent");
        assertNull(value);
    }

    @Test
    void testGetKVConfigNamespaceNotFound() {
        String value = kvService.getKVConfig("non-existent-namespace", "key1");
        assertNull(value);
    }

    @Test
    void testDeleteKVConfig() {
        kvService.putKVConfig("namespace1", "key1", "value1");
        
        kvService.deleteKVConfig("namespace1", "key1");
        
        String value = kvService.getKVConfig("namespace1", "key1");
        assertNull(value);
    }

    @Test
    void testDeleteKVConfigNotFound() {
        assertDoesNotThrow(() -> kvService.deleteKVConfig("namespace1", "non-existent"));
    }

    @Test
    void testMultipleNamespaces() {
        kvService.putKVConfig("ns1", "key1", "value1");
        kvService.putKVConfig("ns2", "key1", "value2");
        
        assertEquals("value1", kvService.getKVConfig("ns1", "key1"));
        assertEquals("value2", kvService.getKVConfig("ns2", "key1"));
    }

    @Test
    void testMultipleKeysPerNamespace() {
        kvService.putKVConfig("ns1", "key1", "value1");
        kvService.putKVConfig("ns1", "key2", "value2");
        kvService.putKVConfig("ns1", "key3", "value3");
        
        assertEquals("value1", kvService.getKVConfig("ns1", "key1"));
        assertEquals("value2", kvService.getKVConfig("ns1", "key2"));
        assertEquals("value3", kvService.getKVConfig("ns1", "key3"));
    }

    @Test
    void testNamespaceOrderTopicConfig() {
        assertEquals("ORDER_TOPIC_CONFIG", KVService.NAMESPACE_ORDER_TOPIC_CONFIG);
    }

    @Test
    void testGetKVListByNamespace() {
        kvService.putKVConfig("ns1", "key1", "value1");
        kvService.putKVConfig("ns1", "key2", "value2");
        
        byte[] result = kvService.getKVListByNamespace("ns1");
        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGetKVListByNamespaceNotFound() {
        byte[] result = kvService.getKVListByNamespace("non-existent");
        assertNull(result);
    }
}