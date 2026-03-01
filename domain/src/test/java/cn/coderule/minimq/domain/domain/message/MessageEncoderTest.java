package cn.coderule.minimq.domain.domain.message;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import cn.coderule.minimq.domain.test.MessageMock;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MessageEncoderTest {

    private MessageEncoder encoder;

    @BeforeEach
    void setUp() {
        MessageConfig messageConfig = new MessageConfig();
        encoder = new MessageEncoder(messageConfig);
    }

    @Test
    void testValidate_ValidMessage() {
        MessageBO messageBO = MessageMock.createMessage();
        var result = MessageEncoder.validate(messageBO);
        assertTrue(result.getLeft());
        assertTrue(result.getRight().isEmpty());
    }

    @Test
    void testValidate_InvalidQueueId() {
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.setQueueId(-1);
        var result = MessageEncoder.validate(messageBO);
        assertFalse(result.getLeft());
        assertTrue(result.getRight().contains("queueId"));
    }

    @Test
    void testValidate_InvalidFlag() {
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.setFlag(-1);
        var result = MessageEncoder.validate(messageBO);
        assertFalse(result.getLeft());
        assertTrue(result.getRight().contains("flag"));
    }

    @Test
    void testValidate_InvalidQueueOffset() {
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.setQueueOffset(-1);
        var result = MessageEncoder.validate(messageBO);
        assertFalse(result.getLeft());
        assertTrue(result.getRight().contains("queueOffset"));
    }

    @Test
    void testValidate_InvalidCommitOffset() {
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.setCommitOffset(-1);
        var result = MessageEncoder.validate(messageBO);
        assertFalse(result.getLeft());
        assertTrue(result.getRight().contains("commitOffset"));
    }

    @Test
    void testValidate_BlankTopic() {
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.setTopic("  ");
        var result = MessageEncoder.validate(messageBO);
        assertFalse(result.getLeft());
        assertTrue(result.getRight().contains("topic"));
    }

    @Test
    void testValidate_MultipleErrors() {
        SocketAddress address = new InetSocketAddress("0.0.0.0", 0);
        MessageBO messageBO = MessageBO.builder()
            .topic("")
            .body(null)
            .queueId(-1)
            .flag(0)
            .queueOffset(0)
            .commitOffset(0)
            .sysFlag(0)
            .bornTimestamp(0)
            .storeTimestamp(0)
            .bornHost(address)
            .storeHost(address)
            .reconsumeTimes(0)
            .prepareOffset(0)
            .build();
        var result = MessageEncoder.validate(messageBO);
        assertFalse(result.getLeft());
        assertEquals(3, result.getRight().size());
    }

    @Test
    void testCalculate_Lengths() {
        MessageBO messageBO = MessageMock.createMessage("TEST_TOPIC", 100);
        encoder.calculate(messageBO);

        assertEquals(10, messageBO.getTopicLength());
        assertEquals(100, messageBO.getBodyLength());
        assertTrue(messageBO.getPropertyLength() >= 0);
        assertTrue(messageBO.getMessageLength() > 0);
    }

    @Test
    void testCalculateMessageLength() {
        MessageBO messageBO = MessageMock.createMessage("TOPIC", 100);
        messageBO.setTopicLength(4);
        messageBO.setBodyLength(100);
        messageBO.setPropertyLength(0);

        int length = MessageEncoder.calculateMessageLength(messageBO);
        assertTrue(length > 0);
    }

    @Test
    void testCalculateMessageLength_V2() {
        MessageBO messageBO = MessageMock.createMessage("TOPIC", 100);
        messageBO.setVersion(MessageVersion.V2);
        messageBO.setTopicLength(4);
        messageBO.setBodyLength(100);
        messageBO.setPropertyLength(0);

        int length = MessageEncoder.calculateMessageLength(messageBO);
        assertTrue(length > 0);
    }

    @Test
    void testEncode_Basic() {
        MessageBO messageBO = MessageMock.createMessage("TEST", 100);
        ByteBuffer buffer = encoder.encode(messageBO);

        assertNotNull(buffer);
        assertTrue(buffer.remaining() > 0);

        buffer.rewind();
        int totalSize = buffer.getInt();
        assertEquals(messageBO.getMessageLength(), totalSize);
    }

    @Test
    void testEncode_FieldsOrder() {
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.setQueueId(1);
        messageBO.setFlag(2);
        messageBO.setQueueOffset(100L);
        messageBO.setCommitOffset(200L);
        messageBO.setSysFlag(3);
        messageBO.setBornTimestamp(1000L);
        messageBO.setStoreTimestamp(2000L);
        messageBO.setReconsumeTimes(5);
        messageBO.setPrepareOffset(300L);

        ByteBuffer buffer = encoder.encode(messageBO);

        buffer.rewind();
        buffer.getInt();
        buffer.getInt();
        buffer.getInt();
        int queueId = buffer.getInt();
        int flag = buffer.getInt();
        long queueOffset = buffer.getLong();
        long commitOffset = buffer.getLong();

        assertEquals(1, queueId);
        assertEquals(2, flag);
        assertEquals(100L, queueOffset);
        assertEquals(200L, commitOffset);
    }

    @Test
    void testEncode_WithProperties() {
        MessageBO messageBO = MessageMock.createMessage("TEST", 50);
        messageBO.setProperties(new HashMap<>());
        messageBO.getProperties().put("KEY1", "VALUE1");

        encoder.calculate(messageBO);
        ByteBuffer buffer = encoder.encode(messageBO);

        assertNotNull(buffer);
        assertTrue(messageBO.getPropertyLength() > 0);
    }

    @Test
    void testEncode_MultipleMessages() {
        MessageBO message1 = MessageMock.createMessage("TOPIC1", 100);
        MessageBO message2 = MessageMock.createMessage("TOPIC2", 200);

        ByteBuffer buffer1 = encoder.encode(message1);
        ByteBuffer buffer2 = encoder.encode(message2);

        assertNotNull(buffer1);
        assertNotNull(buffer2);
    }

    @Test
    void testEncode_V1VsV2() {
        MessageBO v1Message = MessageMock.createMessage("TEST", 100);
        v1Message.setVersion(MessageVersion.V1);

        MessageBO v2Message = MessageMock.createMessage("TEST", 100);
        v2Message.setVersion(MessageVersion.V2);

        encoder.calculate(v1Message);
        encoder.calculate(v2Message);

        ByteBuffer buffer1 = encoder.encode(v1Message);
        encoder.encode(v2Message);

        assertNotNull(buffer1);
    }

    @Test
    void testEncode_LargeBody() {
        MessageBO messageBO = MessageMock.createMessage("TEST", 1024 * 1024);
        ByteBuffer buffer = encoder.encode(messageBO);

        assertNotNull(buffer);
        assertTrue(buffer.remaining() > 1024 * 1024);
    }

    @Test
    void testCalculate_PropertyLength() {
        MessageBO messageBO = MessageMock.createMessage("TEST", 100);
        messageBO.setProperties(new HashMap<>());
        messageBO.getProperties().put("prop1", "value1");
        messageBO.getProperties().put("prop2", "value2");

        encoder.calculate(messageBO);

        assertTrue(messageBO.getPropertyLength() > 0);
    }
}
