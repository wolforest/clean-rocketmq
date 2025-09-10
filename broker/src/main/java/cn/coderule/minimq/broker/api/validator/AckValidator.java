package cn.coderule.minimq.broker.api.validator;

import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;

public class AckValidator {
    public static void validate(AckRequest request) {
        GroupValidator.validate(request.getGroupName());
        TopicValidator.validateTopic(request.getTopicName());

        if (request.getReceiptList().isEmpty()) {
            return;
        }

        request.getReceiptList().forEach(receipt -> {
            if (receipt.getReceiptHandle().isExpired()) {
                throw new InvalidParameterException(
                    InvalidCode.INVALID_RECEIPT_HANDLE,
                    "receipt handle is expired"
                );
            }
        });
    }
}
