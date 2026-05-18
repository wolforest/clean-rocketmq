package cn.coderule.wolfmq.domain.domain.message;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class MessagePOTest {

    @Test
    void testSettersAndGetters() {
        MessagePO po = new MessagePO();
        po.setMessageLength(100);
        po.setMagicCode(1);
        po.setBodyCRC(12345);
        po.setQueueId(0);
        po.setFlag(2);
        po.setQueueOffset(200L);
        po.setCommitLogOffset(300L);
        po.setSysFlag(0);
        po.setBornTimestamp(System.currentTimeMillis());
        po.setBornHost(new InetSocketAddress("localhost", 9876));
        po.setStoreTimestamp(System.currentTimeMillis());
        po.setStoreHost(new InetSocketAddress("localhost", 9877));
        po.setReconsumeTimes(0);
        po.setTransactionOffset(0L);
        po.setBodyLength(50);
        po.setBody("body".getBytes());
        po.setTopicLength(5);
        po.setTopic("topic".getBytes());
        po.setPropertiesLength((short) 10);
        po.setProperties("props".getBytes());
        po.setCrc32(999);

        assertEquals(100, po.getMessageLength());
        assertEquals(1, po.getMagicCode());
        assertEquals(12345, po.getBodyCRC());
        assertEquals(0, po.getQueueId());
        assertEquals(2, po.getFlag());
        assertEquals(200L, po.getQueueOffset());
        assertEquals(300L, po.getCommitLogOffset());
        assertEquals(0, po.getReconsumeTimes());
        assertEquals(999, po.getCrc32());
    }

    @Test
    void testDefaultCommitLogOffset() {
        MessagePO po = new MessagePO();
        assertEquals(0L, po.getCommitLogOffset());
    }
}