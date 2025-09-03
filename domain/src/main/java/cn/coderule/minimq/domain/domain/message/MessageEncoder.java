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

    private final int maxMessageLength;
    private final int propertyCRCLength;


    public MessageEncoder(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;

        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        buffer = allocator.directBuffer(messageConfig.getMaxRequestSize());

        this.maxMessageLength = Integer.MAX_VALUE - messageConfig.getMaxBodySize() >= 64 * 1023
            ? messageConfig.getMaxBodySize() + 64 * 1024
            : Integer.MAX_VALUE;
        this.propertyCRCLength = messageConfig.isEnablePropertyCRC()
            ? messageConfig.getPropertyCRCLength()
            : 0;
    }

    public ByteBuffer encode(MessageBO messageBO) {
        buffer.clear();
        initMessage(messageBO);

        // 1 TOTAL_SIZE
        buffer.writeInt(messageBO.getMessageLength());
        // 2 MAGIC_CODE
        buffer.writeInt(messageBO.getVersion().getMagicCode());
        // 3 BODY_CRC
        buffer.writeInt(messageBO.getBodyCRC());
        // 4 QUEUE_ID
        buffer.writeInt(messageBO.getQueueId());
        // 5 FLAG
        buffer.writeInt(messageBO.getFlag());
        // 6 QUEUE_OFFSET
        buffer.writeLong(messageBO.getQueueOffset());
        // 7 COMMIT_OFFSET
        buffer.writeLong(messageBO.getCommitOffset());
        // 8 SYSFLAG
        buffer.writeInt(messageBO.getSysFlag());
        // 9 BORN_TIMESTAMP
        buffer.writeLong(messageBO.getBornTimestamp());
        // 10 BORN_HOST


        return buffer.nioBuffer(0, messageBO.getMessageLength());
    }

    private void initMessage(MessageBO messageBO) {
        // init topic length
        // int body length
        // init property length
        // init message length
    }

    public static int calculateLength(MessageBO messageBO) {
        return calculateMessageLength(
            messageBO.getVersion(),
            messageBO.getBodyLength(),
            messageBO.getTopicLength(),
            messageBO.getPropertyLength()
        );
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
