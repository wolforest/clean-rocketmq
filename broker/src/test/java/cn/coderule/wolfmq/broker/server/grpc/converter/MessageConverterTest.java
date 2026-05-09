package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.Encoding;
import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.MessageType;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SystemProperties;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageConverterTest {

    private RequestContext createContext(String remoteAddress) {
        return RequestContext.builder()
            .remoteAddress(remoteAddress)
            .build();
    }

    private Message buildMessage(String topic, String messageId, byte[] body) {
        return Message.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .setBody(ByteString.copyFrom(body))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId(messageId)
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.NORMAL)
                .build())
            .build();
    }

    private Message buildMessageWithGroup(String topic, String messageId, byte[] body, String group) {
        return Message.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .setBody(ByteString.copyFrom(body))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId(messageId)
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.NORMAL)
                .setMessageGroup(group)
                .build())
            .build();
    }

    private Message buildFifoMessage(String topic, String messageId, byte[] body) {
        return Message.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .setBody(ByteString.copyFrom(body))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId(messageId)
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.FIFO)
                .build())
            .build();
    }

    private Message buildTransactionMessage(String topic, String messageId, byte[] body) {
        return Message.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .setBody(ByteString.copyFrom(body))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId(messageId)
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.TRANSACTION)
                .build())
            .build();
    }

    private Message buildGzipMessage(String topic, String messageId, byte[] body) {
        return Message.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .setBody(ByteString.copyFrom(body))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId(messageId)
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.GZIP)
                .setMessageType(MessageType.NORMAL)
                .build())
            .build();
    }

    @Test
    void toMessageBO_normalMessage_convertsCorrectly() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildMessage("test_topic", "msg123", "hello".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        MessageBO msgBO = result.get(0);
        assertEquals("test_topic", msgBO.getTopic());
        assertArrayEquals("hello".getBytes(), msgBO.getBody());
    }

    @Test
    void toMessageBO_multipleMessages_convertsAll() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg1 = buildMessage("topic_a", "msg1", "hello1".getBytes());
        Message msg2 = buildMessage("topic_b", "msg2", "hello2".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg1)
            .addMessages(msg2)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(2, result.size());
        assertEquals("topic_a", result.get(0).getTopic());
        assertEquals("topic_b", result.get(1).getTopic());
    }

    @Test
    void toMessageBO_gzipEncoding_setsCompressedFlag() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildGzipMessage("test_topic", "msg123", "compressed_data".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertTrue((result.get(0).getSysFlag() & MessageSysFlag.COMPRESSED_FLAG) != 0);
    }

    @Test
    void toMessageBO_transactionMessage_setsPrepareFlag() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildTransactionMessage("test_topic", "msg_txn", "txn_data".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertTrue((result.get(0).getSysFlag() & MessageSysFlag.PREPARE_MESSAGE) != 0);
    }

    @Test
    void toMessageBO_normalMessage_noSpecialFlags() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildMessage("test_topic", "msg_normal", "data".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getSysFlag() & (MessageSysFlag.COMPRESSED_FLAG | MessageSysFlag.PREPARE_MESSAGE));
    }

    @Test
    void toMessageBO_withRemoteAddress_setsBornHost() {
        RequestContext context = createContext("192.168.1.100:10911");
        Message msg = buildMessage("test_topic", "msg1", "data".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getBornHost());
    }

    @Test
    void toMessageBO_withoutRemoteAddress_setsDefaultBornHost() {
        RequestContext context = createContext(null);
        Message msg = buildMessage("test_topic", "msg1", "data".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getBornHost());
    }

    @Test
    void toMessageBO_setsMessageId() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildMessage("test_topic", "unique-msg-id-123", "data".getBytes());
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getProperties().containsKey(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
        assertEquals("unique-msg-id-123",
            result.get(0).getProperties().get(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
    }

    @Test
    void toMessageBO_withMessageGroup_setsShardingKey() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildMessageWithGroup("test_topic", "msg1", "data".getBytes(), "order_group");
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        Map<String, String> props = result.get(0).getProperties();
        assertEquals("order_group", props.get(MessageConst.PROPERTY_SHARDING_KEY));
    }

    @Test
    void toMessageBO_emptyBody_accepted() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = buildMessage("test_topic", "msg_empty", new byte[0]);
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getBody().length);
    }

    @Test
    void toMessageBO_validUserProperties_accepted() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = Message.newBuilder()
            .setTopic(Resource.newBuilder().setName("test_topic").build())
            .setBody(ByteString.copyFromUtf8("data"))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId("msg_prop")
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.NORMAL)
                .build())
            .putUserProperties(MessageConst.PROPERTY_TAGS, "tag1")
            .putUserProperties(MessageConst.PROPERTY_KEYS, "key1")
            .build();
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getProperties().containsKey(MessageConst.PROPERTY_TAGS));
    }

    @Test
    void toMessageBO_invalidPropertyKey_throwsException() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = Message.newBuilder()
            .setTopic(Resource.newBuilder().setName("test_topic").build())
            .setBody(ByteString.copyFromUtf8("data"))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId("msg_invalid")
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.NORMAL)
                .build())
            .putUserProperties("INVALID_PROPERTY_KEY", "value")
            .build();
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        assertThrows(InvalidRequestException.class,
            () -> MessageConverter.toMessageBO(context, request));
    }

    @Test
    void toMessageBO_blankMessageId_throwsException() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = Message.newBuilder()
            .setTopic(Resource.newBuilder().setName("test_topic").build())
            .setBody(ByteString.copyFromUtf8("data"))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId("")
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.IDENTITY)
                .setMessageType(MessageType.NORMAL)
                .build())
            .build();
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        assertThrows(InvalidRequestException.class,
            () -> MessageConverter.toMessageBO(context, request));
    }

    @Test
    void toMessageBO_transactionAndGzip_setsBothFlags() {
        RequestContext context = createContext("192.168.1.1:10911");
        Message msg = Message.newBuilder()
            .setTopic(Resource.newBuilder().setName("test_topic").build())
            .setBody(ByteString.copyFromUtf8("compressed_txn"))
            .setSystemProperties(SystemProperties.newBuilder()
                .setMessageId("msg_txn_gzip")
                .setBornTimestamp(Timestamps.now())
                .setBodyEncoding(Encoding.GZIP)
                .setMessageType(MessageType.TRANSACTION)
                .build())
            .build();
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .addMessages(msg)
            .build();

        List<MessageBO> result = MessageConverter.toMessageBO(context, request);

        assertEquals(1, result.size());
        int sysFlag = result.get(0).getSysFlag();
        assertTrue((sysFlag & MessageSysFlag.COMPRESSED_FLAG) != 0);
        assertTrue((sysFlag & MessageSysFlag.PREPARE_MESSAGE) != 0);
    }
}