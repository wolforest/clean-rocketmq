package com.wolfmq.domain.domain.message;

import com.wolfmq.domain.domain.message.enums.MessageVersion;
import com.wolfmq.domain.domain.message.utils.MessageContainer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageContainerTest {

    @Test
    void testBuilder() {
        MessageContainer container = MessageContainer.builder()
            .brokerName("broker1")
            .queueId(0)
            .topicKey("topic1")
            .storeSize(256)
            .queueOffset(100L)
            .sysFlag(0)
            .bornTimestamp(System.currentTimeMillis())
            .msgId("msg123")
            .commitLogOffset(500L)
            .bodyCRC(12345)
            .reconsumeTimes(0)
            .preparedTransactionOffset(0L)
            .version(MessageVersion.V1)
            .build();

        assertEquals("broker1", container.getBrokerName());
        assertEquals(0, container.getQueueId());
        assertEquals("topic1", container.getTopicKey());
        assertEquals(256, container.getStoreSize());
        assertEquals(100L, container.getQueueOffset());
        assertEquals("msg123", container.getMsgId());
        assertEquals(500L, container.getCommitLogOffset());
        assertEquals(MessageVersion.V1, container.getVersion());
    }

    @Test
    void testMessageList() {
        MessageContainer container = new MessageContainer();
        assertNotNull(container.getMessageList());
        assertTrue(container.getMessageList().isEmpty());

        MessageBO msg = mock(MessageBO.class);
        container.getMessageList().add(msg);
        assertEquals(1, container.getMessageList().size());
    }
}
