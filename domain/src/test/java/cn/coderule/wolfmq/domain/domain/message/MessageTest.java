package cn.coderule.wolfmq.domain.domain.message;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testBuilder() {
        Message msg = Message.builder()
            .topic("testTopic")
            .flag(1)
            .body("hello".getBytes(StandardCharsets.UTF_8))
            .transactionId("tx1")
            .build();

        assertEquals("testTopic", msg.getTopic());
        assertEquals(1, msg.getFlag());
        assertEquals("hello", msg.getStringBody());
        assertEquals("tx1", msg.getTransactionId());
    }

    @Test
    void testNoArgsConstructor() {
        Message msg = new Message();
        assertNotNull(msg);
    }

    @Test
    void testAllArgsConstructor() {
        HashMap<String, String> props = new HashMap<>();
        props.put("key", "value");
        Message msg = new Message("topic", 2, props, "body".getBytes(StandardCharsets.UTF_8), "tx2");

        assertEquals("topic", msg.getTopic());
        assertEquals(2, msg.getFlag());
        assertEquals("body", msg.getStringBody());
        assertEquals("tx2", msg.getTransactionId());
        assertEquals("value", msg.getProperty("key"));
    }

    @Test
    void testPutAndGetProperty() {
        Message msg = new Message();
        msg.putProperty("key1", "value1");
        assertEquals("value1", msg.getProperty("key1"));
        assertNull(msg.getProperty("nonexistent"));
    }

    @Test
    void testRemoveProperty() {
        Message msg = new Message();
        msg.putProperty("key1", "value1");
        msg.removeProperty("key1");
        assertNull(msg.getProperty("key1"));

        msg.removeProperty("nonexistent");
        assertNull(msg.getProperty("nonexistent"));
    }

    @Test
    void testGetIntProperty() {
        Message msg = new Message();
        assertEquals(-1, msg.getIntProperty("nonexistent"));
        assertEquals(5, msg.getIntProperty("nonexistent", 5));

        msg.putProperty("intKey", "42");
        assertEquals(42, msg.getIntProperty("intKey"));
        assertEquals(42, msg.getIntProperty("intKey", 0));

        msg.putProperty("badInt", "notanumber");
        assertEquals(-1, msg.getIntProperty("badInt"));
    }

    @Test
    void testGetLongProperty() {
        Message msg = new Message();
        assertEquals(-1L, msg.getLongProperty("nonexistent"));
        assertEquals(5L, msg.getLongProperty("nonexistent", 5L));

        msg.putProperty("longKey", "10000000000");
        assertEquals(10000000000L, msg.getLongProperty("longKey"));
    }
}