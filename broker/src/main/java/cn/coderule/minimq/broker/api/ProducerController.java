package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.api.validator.MessageValidator;
import cn.coderule.minimq.broker.domain.producer.Producer;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.exception.InvalidParameterException;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
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
    private final MessageConfig messageConfig;
    private final MessageValidator messageValidator;

    public ProducerController(MessageConfig messageConfig, Producer producer) {
        this.messageConfig = messageConfig;
        this.messageValidator = new MessageValidator(messageConfig);
        this.producer = producer;
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        messageValidator.validate(messageBO);

        return producer.produce(context, messageBO);
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        if (CollectionUtil.isEmpty(messageList)) {
            throw new InvalidParameterException(InvalidCode.BAD_REQUEST, "messageList is empty");
        }

        return producer.produce(context, messageList);
    }
}
