package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.api.validator.MessageValidator;
import cn.coderule.minimq.broker.api.validator.ServerValidator;
import cn.coderule.minimq.broker.domain.producer.Producer;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;
import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.message.MessageIDSetter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ProducerController
 *  - accept RequestContext and MessageBO
 *  - return EnqueueResult
 *  - for multi-protocol support
 */
public class ProducerController {
    private final Producer producer;
    private final BrokerConfig brokerConfig;

    private final MessageValidator messageValidator;
    private final ServerValidator serverValidator;

    public ProducerController(BrokerConfig brokerConfig, Producer producer) {
        this.brokerConfig = brokerConfig;
        this.producer = producer;

        this.serverValidator = new ServerValidator(brokerConfig);
        this.messageValidator = new MessageValidator(brokerConfig.getMessageConfig());
    }

    public void register(RequestContext context, String topicName, ClientChannelInfo channelInfo) {
        this.messageValidator.validateTopic(topicName);
        producer.register(context, topicName, channelInfo);
    }

    public void unregister(RequestContext context, String groupName, ClientChannelInfo channelInfo) {
        producer.unregister(context, groupName, channelInfo);
    }

    public void scanIdleChannels() {
        producer.scanIdleChannels();
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        serverValidator.checkServerReady();
        prepareMessage(messageBO);

        return producer.produce(context, messageBO)
            .whenComplete(
                (result, throwable) -> formatEnqueueResult(result)
            );
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        if (CollectionUtil.isEmpty(messageList)) {
            throw new InvalidParameterException(InvalidCode.BAD_REQUEST, "messageList is empty");
        }

        serverValidator.checkServerReady();
        prepareMessage(messageList);

        return producer.produce(context, messageList);
    }

    private void prepareMessage(List<MessageBO> messageList) {
        for (MessageBO messageBO : messageList) {
            prepareMessage(messageBO);
        }
    }

    private void prepareMessage(MessageBO messageBO) {
        messageValidator.validate(messageBO);
        this.cleanReservedProperty(messageBO);
        this.setMessageId(messageBO);
    }

    private void formatEnqueueResult(EnqueueResult result) {
        if (result == null) {
            return ;
        }

        result.setRegion(brokerConfig.getRegion());
        result.setEnableTrace(brokerConfig.isEnableTrace());
    }

    private void setMessageId(MessageBO messageBO) {
        String messageId = messageBO.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
        if (StringUtil.notBlank(messageId)) {
            return;
        }

        messageId = MessageIDSetter.createUniqID();
        messageBO.putProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, messageId);
    }

    public void cleanReservedProperty(MessageBO messageBO) {
        messageBO.removeProperty(MessageConst.PROPERTY_POP_CK);
    }
}
