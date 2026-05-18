package cn.coderule.wolfmq.domain.domain.message;

import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.enums.message.MessageStatus;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class MessageBOTest {

    @Test
    void testBuilder() {
        MessageBO msg = MessageBO.builder()
            .topic("testTopic")
            .flag(1)
            .queueId(0)
            .messageLength(256)
            .body("hello".getBytes())
            .build();

        assertEquals("testTopic", msg.getTopic());
        assertEquals(1, msg.getFlag());
        assertEquals(0, msg.getQueueId());
    }

    @Test
    void testNotFound() {
        MessageBO msg = MessageBO.notFound();
        assertEquals(MessageStatus.NO_MATCHED_MESSAGE, msg.getStatus());
    }

    @Test
    void testSetBody() {
        MessageBO msg = new MessageBO();
        msg.setBody("world".getBytes());
        assertEquals("world".getBytes().length, msg.getBodyLength());
    }

    @Test
    void testIsValid() {
        MessageBO msg = MessageBO.notFound();
        assertFalse(msg.isValid());

        msg.setStatus(MessageStatus.FOUND);
        msg.setMessageLength(100);
        assertTrue(msg.isValid());
    }

    @Test
    void testIsEmpty() {
        MessageBO msg = new MessageBO();
        assertTrue(msg.isEmpty());

        msg.setMessageLength(-1);
        assertTrue(msg.isEmpty());

        msg.setMessageLength(0);
        assertTrue(msg.isEmpty());

        msg.setMessageLength(100);
        assertFalse(msg.isEmpty());
    }

    @Test
    void testProducerGroup() {
        MessageBO msg = new MessageBO();
        msg.setProducerGroup("pg1");
        assertEquals("pg1", msg.getProducerGroup());
    }

    @Test
    void testRealTopicAndQueue() {
        MessageBO msg = new MessageBO();
        msg.setTopic("originalTopic");
        msg.setQueueId(5);
        msg.setSystemQueue("systemTopic", 0);

        assertEquals("originalTopic", msg.getRealTopic());
        assertEquals(5, msg.getRealQueueId());
        assertEquals(0, msg.getQueueId());
        assertEquals("systemTopic", msg.getTopic());
    }

    @Test
    void testGetBodyString() {
        MessageBO msg = new MessageBO();
        assertEquals("", msg.getBodyString());

        msg.setBody("test".getBytes());
        assertEquals("test", msg.getBodyString());
    }

    @Test
    void testBornHostBytes() {
        MessageBO msg = new MessageBO();
        assertNull(msg.getBornHostBytes());

        msg.setBornHost(new InetSocketAddress("localhost", 9876));
        assertNotNull(msg.getBornHostBytes());
    }
}