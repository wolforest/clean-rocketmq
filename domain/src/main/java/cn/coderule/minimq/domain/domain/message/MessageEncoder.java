package cn.coderule.minimq.domain.domain.message;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import cn.coderule.minimq.domain.core.exception.EnqueueException;
import cn.coderule.minimq.domain.utils.message.MessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder {
    private final MessageConfig messageConfig;

    private final ByteBuf buffer;

    private MessageBO messageBO;
    @Getter
    private int messageLength = 0;

    public MessageEncoder(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;

        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        buffer = allocator.directBuffer(messageConfig.getMaxRequestSize());
    }

    public void setMessage(MessageBO messageBO) {
        buffer.clear();
        this.messageBO = messageBO;
    }

    public static int calculateLength(MessageBO messageBO) {
        return calculateMessageLength(
            messageBO.getVersion(),
            messageBO.getBodyLength(),
            messageBO.getTopicLength(),
            messageBO.getPropertyLength()
        );
    }

    public ByteBuffer encode() {
        String propertiesString = MessageUtils.propertiesToString(messageBO.getProperties());
        byte[] properties = propertiesString.getBytes(StandardCharsets.UTF_8);
        if (properties.length > Short.MAX_VALUE) {
            throw new EnqueueException(EnqueueStatus.PROPERTIES_SIZE_EXCEEDED);
        }

        byte[] topic = messageBO.getTopic().getBytes(StandardCharsets.UTF_8);
        int topicLen = topic.length;
        int bodyLen = messageBO.getBody().length;
        this.messageLength = calculateMessageLength(
            messageBO.getVersion(), bodyLen, topicLen, properties.length
        );

        if (messageLength > messageConfig.getMaxRequestSize() || bodyLen > messageConfig.getMaxBodySize()) {
            throw new EnqueueException(EnqueueStatus.MESSAGE_ILLEGAL);
        }

        return getByteBuffer();
    }

    private ByteBuffer getByteBuffer() {
        return this.buffer.nioBuffer(0, this.messageLength);
    }

    private static int calculateMessageLength(MessageVersion messageVersion,
        int bodyLength, int topicLength, int propertiesLength) {

        int bornHostLength = 8;
        int storeHostAddressLength = 8;

        return 4 //TOTAL_SIZE
            + 4 //MAGIC_CODE
            + 4 //BODY_CRC
            + 4 //QUEUE_ID
            + 4 //FLAG
            + 8 //QUEUE_OFFSET
            + 8 //COMMITLOG_OFFSET
            + 4 //SYSFLAG
            + 8 //BORN_TIMESTAMP
            + bornHostLength //BORN_HOST
            + 8 //STORE_TIMESTAMP
            + storeHostAddressLength //STORE_HOST_ADDRESS
            + 4 //RECONSUME_TIMES
            + 8 //Prepared Transaction Offset
            + 4 + bodyLength //BODY
            + messageVersion.getTopicLengthSize() + topicLength //TOPIC
            + 2 + propertiesLength; //propertiesLength
    }
}
