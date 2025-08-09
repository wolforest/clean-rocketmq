package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.Encoding;
import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.MessageType;
import apache.rocketmq.v2.SendMessageRequest;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.core.exception.RequestException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.coderule.common.util.lang.string.StringUtil.containControlCharacter;

public class MessageConverter {

    public static List<MessageBO> toMessageBO(RequestContext context, SendMessageRequest request) {
        List<MessageBO> messageList = new ArrayList<>();

        for (Message rpcMsg : request.getMessagesList()) {
            validateProperty(rpcMsg.getUserPropertiesMap());

            String topic = rpcMsg.getTopic().getName();
            MessageBO messageBO = MessageBO.builder()
                .topic(topic)
                .body(rpcMsg.getBody().toByteArray())
                .sysFlag(buildSysFlag(rpcMsg))
                .properties(buildProperties(context, rpcMsg, topic))
                .build();

            messageList.add(messageBO);
        }

        return messageList;
    }

    private static void validateProperty(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!MessageConst.STRING_HASH_SET.contains(entry.getKey())) {
                throw new RequestException(
                    InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
                    "property key " + entry.getKey() + " is not allowed"
                );
            }

            if (containControlCharacter(entry.getKey())) {
                throw new RequestException(
                    InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
                    "property key " + entry.getKey() + " can't contain control character"
                );
            }

            if (containControlCharacter(entry.getValue())) {
                throw new RequestException(
                    InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
                    "property value " + entry.getValue() + " can't contain control character"
                );
            }
        }
    }

    private static int buildSysFlag(Message message) {
        // sysFlag (body encoding & message type)
        int sysFlag = 0;
        Encoding bodyEncoding = message.getSystemProperties().getBodyEncoding();
        if (bodyEncoding.equals(Encoding.GZIP)) {
            sysFlag |= MessageSysFlag.COMPRESSED_FLAG;
        }

        // transaction
        MessageType messageType = message.getSystemProperties().getMessageType();
        if (messageType.equals(MessageType.TRANSACTION)) {
            sysFlag |= MessageSysFlag.TRANSACTION_PREPARED_TYPE;
        }

        return sysFlag;
    }

    private static Map<String, String> buildProperties(RequestContext context, Message rpcMsg, String producerGroup) {
        Map<String, String> properties = new HashMap<>(
            rpcMsg.getUserPropertiesMap()
        );

        setMessageId(properties, rpcMsg);
        setTag(properties, rpcMsg);
        setKeys(properties, rpcMsg);
        setGroup(properties, rpcMsg, producerGroup);

        setTransactionProperty(properties, rpcMsg);
        setDelayProperty(properties, rpcMsg);
        setReconsumeTimes(properties, rpcMsg);

        setTraceContext(properties, rpcMsg);
        setBornHost(context, properties, rpcMsg);
        setBornTime(properties, rpcMsg);

        return properties;
    }

    private static void setMessageId(Map<String, String> properties, Message message) {
        String messageId = message.getSystemProperties().getMessageId();
        if (StringUtil.isBlank(messageId)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_ID, "message id can not be blank");
        }

        properties.put(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, messageId);
    }

    private static void setTag(Map<String, String> properties, Message message) {
        String tag = message.getSystemProperties().getTag();

        properties.put(MessageConst.PROPERTY_TAGS, tag);
    }

    private static void setKeys(Map<String, String> properties, Message message) {
        List<String> keyList = message.getSystemProperties().getKeysList();

        for (String key : keyList) {
            validateMessageKey(key);
        }

        String keys = String.join(MessageConst.KEY_SEPARATOR, keyList);
        properties.put(MessageConst.PROPERTY_KEYS, keys);
    }

    private static void validateMessageKey(String key) {
        if (StringUtil.isEmpty(key)) {
            return;
        }

        if (StringUtil.isBlank(key)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_KEY, "message can't be blank");
        }

        if (containControlCharacter(key)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_KEY, "message key can't contain control character");
        }
    }

    private static void setGroup(Map<String, String> properties, Message message, String producerGroup) {
        properties.put(MessageConst.PROPERTY_PRODUCER_GROUP, producerGroup);

        String group = message.getSystemProperties().getMessageGroup();
        if (StringUtil.isBlank(group)) {
            return;
        }

        properties.put(MessageConst.PROPERTY_SHARDING_KEY, group);
    }

    private static void setTransactionProperty(Map<String, String> properties, Message message) {
        MessageType messageType = message.getSystemProperties().getMessageType();
        if (!messageType.equals(MessageType.TRANSACTION)) {
            return;
        }

        properties.put(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        if (!message.getSystemProperties().hasOrphanedTransactionRecoveryDuration()) {
            return;
        }

        long duration = Durations.toSeconds(
            message.getSystemProperties().getOrphanedTransactionRecoveryDuration()
        );

        properties.put(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS, String.valueOf(duration));
    }

    private static void setDelayProperty(Map<String, String> properties, Message message) {
        if (!message.getSystemProperties().hasDeliveryTimestamp()) {
            return;
        }

        Timestamp timestamp = message.getSystemProperties().getDeliveryTimestamp();
        long delayTime = Timestamps.toMillis(timestamp);
        properties.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, String.valueOf(delayTime));
    }

    private static void setReconsumeTimes(Map<String, String> properties, Message message) {
        int reconsumeTimes = message.getSystemProperties().getDeliveryAttempt();
        properties.put(MessageConst.PROPERTY_RECONSUME_TIME, String.valueOf(reconsumeTimes));
    }

    private static void setBornHost(RequestContext context, Map<String, String> properties, Message message) {
        String bornHost = message.getSystemProperties().getBornHost();
        if (StringUtil.isBlank(bornHost)) {
            bornHost = context.getRemoteAddress();
        }

        if (StringUtil.notBlank(bornHost)) {
            properties.put(MessageConst.PROPERTY_BORN_HOST, bornHost);
        }
    }

    private static void setBornTime(Map<String, String> properties, Message message) {
        Timestamp bornTime = message.getSystemProperties().getBornTimestamp();
        if (!Timestamps.isValid(bornTime)) {
            bornTime = Timestamps.now();
        }

        properties.put(MessageConst.PROPERTY_BORN_TIMESTAMP, String.valueOf(Timestamps.toMillis(bornTime)));
    }

    private static void setTraceContext(Map<String, String> properties, Message message) {
        String traceContext = message.getSystemProperties().getTraceContext();
        if (StringUtil.isBlank(traceContext)) {
            return;
        }

        properties.put(MessageConst.PROPERTY_TRACE_CONTEXT, traceContext);
    }

}
