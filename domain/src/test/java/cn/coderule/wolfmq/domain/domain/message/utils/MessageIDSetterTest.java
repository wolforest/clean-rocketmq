package cn.coderule.wolfmq.domain.domain.message.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageIDSetterTest {

    @Test
    void createUniqID_returnsNonNull() {
        String id = MessageIDSetter.createUniqID();
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    void createUniqID_uniqueAcrossCalls() {
        String id1 = MessageIDSetter.createUniqID();
        String id2 = MessageIDSetter.createUniqID();
        assertNotEquals(id1, id2);
    }

    @Test
    void createUniqID_consistentLength() {
        String id1 = MessageIDSetter.createUniqID();
        String id2 = MessageIDSetter.createUniqID();
        assertEquals(id1.length(), id2.length());
    }

    @Test
    void createFakeIP_returns4Bytes() {
        byte[] ip = MessageIDSetter.createFakeIP();
        assertNotNull(ip);
        assertEquals(4, ip.length);
    }

    @Test
    void getIPFromID_roundtrip() {
        String id = MessageIDSetter.createUniqID();
        byte[] ip = MessageIDSetter.getIPFromID(id);
        assertNotNull(ip);
        assertTrue(ip.length == 4 || ip.length == 16);
    }

    @Test
    void getIPStrFromID_returnsIpString() {
        String id = MessageIDSetter.createUniqID();
        String ipStr = MessageIDSetter.getIPStrFromID(id);
        assertNotNull(ipStr);
        assertFalse(ipStr.isEmpty());
    }

    @Test
    void getPidFromID_returnsNonNegative() {
        String id = MessageIDSetter.createUniqID();
        int pid = MessageIDSetter.getPidFromID(id);
        assertTrue(pid >= 0);
    }
}