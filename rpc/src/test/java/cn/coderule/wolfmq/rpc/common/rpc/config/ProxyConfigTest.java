package cn.coderule.wolfmq.rpc.common.rpc.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProxyConfigTest {

    @Test
    void testNoArgsConstructor() {
        ProxyConfig config = new ProxyConfig();
        assertNotNull(config);
    }

    @Test
    void testConstructorWithAddr() {
        ProxyConfig config = new ProxyConfig("localhost:9876");
        assertEquals("localhost:9876", config.getAddr());
    }

    @Test
    void testAllArgsConstructor() {
        ProxyConfig config = new ProxyConfig("addr", "user", "pass");
        assertEquals("addr", config.getAddr());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
    }

    @Test
    void testSetters() {
        ProxyConfig config = new ProxyConfig();
        config.setAddr("addr");
        config.setUsername("user");
        config.setPassword("pass");

        assertEquals("addr", config.getAddr());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
    }
}