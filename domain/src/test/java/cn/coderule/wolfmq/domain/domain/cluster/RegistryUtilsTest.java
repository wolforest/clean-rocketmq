package cn.coderule.wolfmq.domain.domain.cluster;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistryUtilsTest {

    @Test
    void testConstants() {
        assertEquals("MQ_INST_", RegistryUtils.INSTANCE_PREFIX);
        assertEquals("NAMESRV_ADDR", RegistryUtils.NAMESRV_ADDR_ENV);
        assertEquals("rocketmq.namesrv.addr", RegistryUtils.NAMESRV_ADDR_PROPERTY);
    }

    @Test
    void testValidateEndpoint() {
        assertTrue(RegistryUtils.validateEndpoint("MQ_INST_xx_yy.registry.com"));
        assertTrue(RegistryUtils.validateEndpoint("http://MQ_INST_xx_yy.registry.com"));
        assertTrue(RegistryUtils.validateEndpoint("https://MQ_INST_a1_b2.registry.com"));

        assertFalse(RegistryUtils.validateEndpoint(""));
        assertFalse(RegistryUtils.validateEndpoint("invalid"));
        assertFalse(RegistryUtils.validateEndpoint("http://example.com"));
    }

    @Test
    void testParseEndpoint() {
        assertEquals("MQ_INST_xx_yy", RegistryUtils.parseEndpoint("MQ_INST_xx_yy.registry.com"));
        assertEquals("MQ_INST_xx_yy", RegistryUtils.parseEndpoint("http://MQ_INST_xx_yy.registry.com"));
        assertNull(RegistryUtils.parseEndpoint(""));
        assertNull(RegistryUtils.parseEndpoint(null));
    }

    @Test
    void testGeEndpoint() {
        assertEquals("addr", RegistryUtils.geEndpoint("http://addr"));
        assertEquals("addr:9876", RegistryUtils.geEndpoint("http://addr:9876"));
        assertNull(RegistryUtils.geEndpoint(""));
        assertNull(RegistryUtils.geEndpoint(null));
    }
}