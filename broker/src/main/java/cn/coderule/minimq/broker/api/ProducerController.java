package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.api.validator.MessageValidator;
import cn.coderule.minimq.broker.api.validator.ServerValidator;
import cn.coderule.minimq.broker.domain.producer.Producer;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.exception.InvalidParameterException;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageIDSetter;
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

    public ProducerController(BrokerConfig brokerConfig, MessageConfig messageConfig, Producer producer) {
        this.brokerConfig = brokerConfig;
        this.producer = producer;

        this.serverValidator = new ServerValidator(brokerConfig);
        this.messageValidator = new MessageValidator(messageConfig);
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        serverValidator.checkServerReady();
        messageValidator.validate(messageBO);

        this.cleanReservedProperty(messageBO);
        this.setMessageId(messageBO);

        return producer.produce(context, messageBO)
            .whenComplete(
                (result, throwable) -> formatEnqueueResult(result)
            );
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        if (CollectionUtil.isEmpty(messageList)) {
            throw new InvalidParameterException(InvalidCode.BAD_REQUEST, "messageList is empty");
        }

        return producer.produce(context, messageList);
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
        messageBO.deleteProperty(MessageConst.PROPERTY_POP_CK);
    }
}
