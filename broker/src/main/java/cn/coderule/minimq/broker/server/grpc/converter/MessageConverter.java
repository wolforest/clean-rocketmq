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


        return properties;
    }

    private static void setMessageId(Map<String, String> properties, Message message) {
        String messageId = message.getSystemProperties().getMessageId();
        if (StringUtil.isBlank(messageId)) {
            throw new GrpcException(InvalidCode.ILLEGAL_MESSAGE_ID, "message id can not be blank");
        }
        properties.put(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, messageId);
    }


}
