package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.Encoding;
import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.MessageType;
import apache.rocketmq.v2.SendMessageRequest;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConverter {

    public static List<MessageBO> toMessageBO(RequestContext context, SendMessageRequest request) {
        List<MessageBO> result = new ArrayList<>();

        for (Message message : request.getMessagesList()) {
            String topic = message.getTopic().getName();
            MessageBO messageBO = MessageBO.builder()
                .topic(topic)
                .body(message.getBody().toByteArray())
                .sysFlag(buildSysFlag(message))
                .properties(buildProperties(context, message, topic))
                .build();

            result.add(messageBO);
        }

        return result;
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

    private static Map<String, String> buildProperties(RequestContext context, Message message, String producerGroup) {
        Map<String, String> properties = new HashMap<>(
            message.getUserPropertiesMap()
        );

        setMessageId(properties, message);
        setGroup(properties, message, producerGroup);

        setTransactionProperty(properties, message);



        return properties;
    }

    private static void setMessageId(Map<String, String> properties, Message message) {
        String messageId = message.getSystemProperties().getMessageId();
        if (StringUtil.isBlank(messageId)) {
            throw new GrpcException(InvalidCode.ILLEGAL_MESSAGE_ID, "message id can not be blank");
        }
        properties.put(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, messageId);
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
            return;
        }

        properties.put(MessageConst.PROPERTY_BORN_TIMESTAMP, String.valueOf(Timestamps.toMillis(bornTime)));
    }

    private static void setTag(Map<String, String> properties, Message message) {

    }

    private static void setKeys(Map<String, String> properties, Message message) {

    }



    private static void setTraceContext(Map<String, String> properties, Message message) {
        String traceContext = message.getSystemProperties().getTraceContext();
        if (StringUtil.isBlank(traceContext)) {
            return;
        }

        properties.put(MessageConst.PROPERTY_TRACE_CONTEXT, traceContext);
    }

}
