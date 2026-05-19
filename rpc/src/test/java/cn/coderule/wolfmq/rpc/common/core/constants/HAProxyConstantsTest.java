package cn.coderule.wolfmq.rpc.common.core.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HAProxyConstantsTest {

    @Test
    void testChannelIdConstant() {
        assertEquals("channel_id", HAProxyConstants.CHANNEL_ID);
    }

    @Test
    void testProxyProtocolPrefix() {
        assertEquals("proxy_protocol_", HAProxyConstants.PROXY_PROTOCOL_PREFIX);
    }

    @Test
    void testProxyProtocolAddr() {
        assertEquals("proxy_protocol_addr", HAProxyConstants.PROXY_PROTOCOL_ADDR);
    }

    @Test
    void testProxyProtocolPort() {
        assertEquals("proxy_protocol_port", HAProxyConstants.PROXY_PROTOCOL_PORT);
    }

    @Test
    void testProxyProtocolTlvPrefix() {
        assertEquals("proxy_protocol_tlv_0x", HAProxyConstants.PROXY_PROTOCOL_TLV_PREFIX);
    }

    @Test
    void testDerivedConstants() {
        assertEquals("proxy_protocol_" + "addr", HAProxyConstants.PROXY_PROTOCOL_ADDR);
    }
}