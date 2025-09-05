package cn.coderule.minimq.domain.domain.message;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder {
    private final ByteBuf buffer;
    private final int propertyCRCLength;

    public MessageEncoder(MessageConfig messageConfig) {
        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        buffer = allocator.directBuffer(messageConfig.getMaxRequestSize());

        this.propertyCRCLength = messageConfig.isEnablePropertyCRC()
            ? messageConfig.getPropertyCRCLength()
            : 0;
    }

    public static int calculateMessageLength(MessageBO messageBO) {
        return calculateMessageLength(
            messageBO.getVersion(),
            messageBO.getBodyLength(),
            messageBO.getTopicLength(),
            messageBO.getPropertyLength()
        );
    }

    public static Pair<Boolean, Set<String>> validate(MessageBO messageBO) {
        boolean status = true;
        Set<String> errorKeys = new HashSet<>();

        if (messageBO.getQueueId() < 0) {
            status = false;
            errorKeys.add("queueId");
        }

        if (messageBO.getFlag() < 0) {
            status = false;
            errorKeys.add("flag");
        }

        if (messageBO.getQueueOffset() < 0) {
            status = false;
            errorKeys.add("queueOffset");
        }

        if (messageBO.getCommitOffset() < 0) {
            status = false;
            errorKeys.add("commitOffset");
        }

        if (messageBO.getSysFlag() < 0) {
            status = false;
            errorKeys.add("sysFlag");
        }

        if (messageBO.getBornTimestamp() < 0) {
            status = false;
            errorKeys.add("bornTimestamp");
        }

        if (messageBO.getStoreTimestamp() < 0) {
            status = false;
            errorKeys.add("storeTimestamp");
        }

        if (null == messageBO.getBornHostBytes()) {
            status = false;
            errorKeys.add("bornHost");
        }

        if (null == messageBO.getStoreHostBytes()) {
            status = false;
            errorKeys.add("storeHost");
        }

        if (messageBO.getReconsumeTimes() < 0) {
            status = false;
            errorKeys.add("reconsumeTimes");
        }

        if (messageBO.getPreparedTransactionOffset() < 0) {
            status = false;
            errorKeys.add("preparedTransactionOffset");
        }

        if (StringUtil.isBlank(messageBO.getTopic())) {
            status = false;
            errorKeys.add("topic");
        }

        if (null == messageBO.getBody()) {
            status = false;
            errorKeys.add("body");
        }

        return Pair.of(status, errorKeys);
    }

    private void calculate(MessageBO messageBO) {
        // init topic length
        if (messageBO.getTopicLength() < 0) {
            messageBO.setTopicLength(
                messageBO.getTopic().getBytes(MQConstants.MQ_CHARSET).length
            );
        }
        // int body length
        if (messageBO.getBodyLength() < 0) {
            messageBO.setBodyLength(
                messageBO.getBody() == null ? 0 : messageBO.getBody().length
            );
        }

        // init property length
        if (messageBO.getPropertyLength() < 0) {
            calculatePropertyLength(messageBO);
        }

        // init message length
        if (messageBO.getMessageLength() < 0) {
            messageBO.setMessageLength(
                calculateMessageLength(messageBO)
            );
        }
    }

    public ByteBuffer encode(MessageBO messageBO) {
        buffer.clear();
        calculate(messageBO);

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
        buffer.writeBytes(messageBO.getBornHostBytes());
        // 11 STORE_TIMESTAMP
        buffer.writeLong(messageBO.getStoreTimestamp());
        // 12 STORE_HOST
        buffer.writeBytes(messageBO.getStoreHostBytes());
        // 13 RECONSUME_TIMES
        buffer.writeInt(messageBO.getReconsumeTimes());
        // 14 Prepared Transaction Offset
        buffer.writeLong(messageBO.getPreparedTransactionOffset());
        // 15 BODY
        writeBody(messageBO);
        // 16 TOPIC
        writeTopic(messageBO);
        // 17 PROPERTIES
        writeProperty(messageBO);
        // 18 CRC
        buffer.writerIndex(buffer.writerIndex() + propertyCRCLength);

        return buffer.nioBuffer(0, messageBO.getMessageLength());
    }

    private void writeBody(MessageBO messageBO) {
        buffer.writeInt(messageBO.getBodyLength());
        if (messageBO.getBodyLength() > 0) {
            buffer.writeBytes(messageBO.getBody());
        }
    }

    private void writeTopic(MessageBO messageBO) {
        if (MessageVersion.V2.equals(messageBO.getVersion())) {
            buffer.writeShort((short) messageBO.getTopicLength());
        } else {
            buffer.writeByte((byte) messageBO.getTopicLength());
        }

        buffer.writeBytes(
            messageBO.getTopic().getBytes(MQConstants.MQ_CHARSET)
        );
    }

    private void writeProperty(MessageBO messageBO) {
        buffer.writeShort((short) messageBO.getPropertyLength());
        if (messageBO.getPropertyLength() > propertyCRCLength) {
            buffer.writeBytes(
                messageBO.getPropertiesString().getBytes(MQConstants.MQ_CHARSET)
            );
        }

        if (messageBO.isAppendPropertyCRC()) {
            buffer.writeByte(MessageConst.PROPERTY_SEPARATOR);
        }
    }



    private void calculatePropertyLength(MessageBO messageBO) {
        byte[] properties = messageBO.getPropertiesString().getBytes(MQConstants.MQ_CHARSET);
        int propertyLength = properties.length;

        if (propertyLength > 0
            && propertyCRCLength > 0
            && properties[propertyLength - 1] != MessageConst.PROPERTY_SEPARATOR
        ) {
            propertyLength += 1;
            messageBO.setAppendPropertyCRC(true);
        } else {
            messageBO.setAppendPropertyCRC(false);
        }

        propertyLength += propertyCRCLength;
        messageBO.setPropertyLength(propertyLength);
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
