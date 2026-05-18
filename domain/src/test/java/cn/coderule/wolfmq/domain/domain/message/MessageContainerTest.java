package cn.coderule.wolfmq.domain.domain.message;

import cn.coderule.wolfmq.domain.core.enums.message.MessageVersion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageContainerTest {

    @Test
    void testBuilder() {
        MessageBO msg = mock(MessageBO.class);

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

    @Test
    void testNoArgsConstructor() {
        MessageContainer container = new MessageContainer();
        assertNotNull(container);
    }
}