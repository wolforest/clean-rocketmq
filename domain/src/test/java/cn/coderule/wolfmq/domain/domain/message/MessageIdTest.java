package cn.coderule.wolfmq.domain.domain.message;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class MessageIdTest {

    @Test
    void testConstructor_setsFields() {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
        MessageId messageId = new MessageId(address, 100L);

        assertEquals(address, messageId.getAddress());
        assertEquals(100L, messageId.getOffset());
    }

    @Test
    void testSetAddress() {
        InetSocketAddress address = new InetSocketAddress("192.168.1.1", 9090);
        MessageId messageId = new MessageId(null, 0L);

        messageId.setAddress(address);

        assertEquals(address, messageId.getAddress());
    }

    @Test
    void testSetOffset() {
        MessageId messageId = new MessageId(null, 0L);

        messageId.setOffset(500L);

        assertEquals(500L, messageId.getOffset());
    }

    @Test
    void testEquals_sameAddressAndOffset() {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
        MessageId id1 = new MessageId(address, 100L);
        MessageId id2 = new MessageId(address, 100L);

        assertNotEquals(id1, id2);
    }
}