package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.Encoding;
import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.MessageType;
import apache.rocketmq.v2.SendMessageRequest;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.enums.InvalidCode;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import com.google.protobuf.util.Durations;
import java.util.HashMap;
import java.util.Map;

public class MessageConverter {

    public static MessageBO toMessageBO(RequestContext context, SendMessageRequest request) {
        Message message = request.getMessages(0);

        return MessageBO.builder()
            .topic(message.getTopic().getName())
            .body(message.getBody().toByteArray())

            .sysFlag(buildSysFlag(message))
            .build();
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


    private static void setBornHost(Map<String, String> properties, Message message) {

    }

    private static void setBornTime(Map<String, String> properties, Message message) {

    }

    private static void setTag(Map<String, String> properties, Message message) {

    }

    private static void setKeys(Map<String, String> properties, Message message) {

    }

    private static void setGroup(Map<String, String> properties, Message message) {

    }

    private static void setTraceContext(Map<String, String> properties, Message message) {

    }

}
