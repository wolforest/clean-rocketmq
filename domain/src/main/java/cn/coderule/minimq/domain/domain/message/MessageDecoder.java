package cn.coderule.minimq.domain.domain.message;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import cn.coderule.minimq.domain.utils.message.MessageUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MessageDecoder {
    public static MessageBO decode(ByteBuffer byteBuffer) {
        return decode(byteBuffer, false);
    }

    public static MessageBO decode(ByteBuffer byteBuffer, boolean checkCRC) {
        try {
            MessageBO msg = new MessageBO();

            // 1 TOTAL_SIZE
            int storeSize = byteBuffer.getInt();
            msg.setMessageLength(storeSize);

            // 2 MAGIC_CODE
            int magicCode = byteBuffer.getInt();
            msg.setMagicCode(magicCode);
            MessageVersion version = MessageVersion.valueOfMagicCode(magicCode);

            // 3 BODY_CRC
            int bodyCRC = byteBuffer.getInt();
            msg.setBodyCRC(bodyCRC);

            // 4 QUEUE_ID
            int queueId = byteBuffer.getInt();
            msg.setQueueId(queueId);

            // 5 FLAG
            int flag = byteBuffer.getInt();
            msg.setFlag(flag);

            // 6 QUEUE_OFFSET
            long queueOffset = byteBuffer.getLong();
            msg.setQueueOffset(queueOffset);

            // 7 COMMIT_OFFSET
            long physicOffset = byteBuffer.getLong();
            msg.setCommitOffset(physicOffset);

            // 8 SYSFLAG
            int sysFlag = byteBuffer.getInt();
            msg.setSysFlag(sysFlag);

            // 9 BORN_TIMESTAMP
            long bornTimeStamp = byteBuffer.getLong();
            msg.setBornTimestamp(bornTimeStamp);

            // 10 BORN_HOST
            int bornHostIPLength = 4;
            byte[] bornHost = new byte[bornHostIPLength];
            byteBuffer.get(bornHost, 0, bornHostIPLength);
            int port = byteBuffer.getInt();
            msg.setBornHost(new InetSocketAddress(InetAddress.getByAddress(bornHost), port));

            // 11 STORE_TIMESTAMP
            long storeTimestamp = byteBuffer.getLong();
            msg.setStoreTimestamp(storeTimestamp);

            // 12 STORE_HOST
            int storeHostIPLength = 4;
            byte[] storeHost = new byte[storeHostIPLength];
            byteBuffer.get(storeHost, 0, storeHostIPLength);
            port = byteBuffer.getInt();
            msg.setStoreHost(new InetSocketAddress(InetAddress.getByAddress(storeHost), port));

            // 13 RECONSUME_TIMES
            int reconsumeTimes = byteBuffer.getInt();
            msg.setReconsumeTimes(reconsumeTimes);

            // 14 Prepared Transaction Offset
            long preparedTransactionOffset = byteBuffer.getLong();
            msg.setPreparedTransactionOffset(preparedTransactionOffset);

            // 15 BODY
            int bodyLen = byteBuffer.getInt();
            if (bodyLen > 0) {
                byte[] body = new byte[bodyLen];
                byteBuffer.get(body);

                if (checkCRC) {
                    //crc body
                    int crc = HashUtil.crc32(body);
                    if (crc != bodyCRC) {
                        throw new Exception("Msg crc is error!");
                    }
                }

                msg.setBody(body);
            }

            // 16 TOPIC
            int topicLen = version.getTopicLength(byteBuffer);
            byte[] topic = new byte[topicLen];
            byteBuffer.get(topic);
            msg.setTopic(new String(topic, StandardCharsets.UTF_8));

            // 17 properties
            short propertiesLength = byteBuffer.getShort();
            if (propertiesLength > 0) {
                byte[] properties = new byte[propertiesLength];
                byteBuffer.get(properties);

                String propertiesString = new String(properties, StandardCharsets.UTF_8);
                Map<String, String> map = MessageUtils.stringToProperties(propertiesString);
                msg.setProperties(map);
            }

            int msgIDLength = storeHostIPLength + 4 + 8;
            ByteBuffer byteBufferMsgId = ByteBuffer.allocate(msgIDLength);
            String msgId = createMessageId(byteBufferMsgId, msg.getStoreHostBuffer(), msg.getCommitOffset());
            msg.setMessageId(msgId);

            msg.setStatus(MessageStatus.FOUND);
            return msg;
        } catch (Exception e) {
            byteBuffer.position(byteBuffer.limit());
        }

        return MessageBO.notFound();
    }

    private static String createMessageId(final ByteBuffer input, final ByteBuffer addr, final long offset) {
        input.flip();
        int msgIDLength = addr.limit() == 8 ? 16 : 28;
        input.limit(msgIDLength);

        input.put(addr);
        input.putLong(offset);
        return StringUtil.bytes2string(input.array());
    }

}
