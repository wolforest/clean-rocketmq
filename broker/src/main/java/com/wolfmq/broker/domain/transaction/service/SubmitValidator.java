package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.domain.config.business.TransactionConfig;
import com.wolfmq.domain.core.constant.MessageConst;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.transaction.SubmitRequest;

public class SubmitValidator {
    private final TransactionConfig transactionConfig;

    public SubmitValidator(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;
    }

    public void validate(SubmitRequest request, MessageBO message) {
        if (message == null) {
            throw new InvalidRequestException(
                InvalidCode.MESSAGE_NOT_FOUND,
                "Can't find transaction message, while commit"
            );
        }

        validateCheckTime(request, message);
        validateProducerGroup(request, message);
        validateCommitOffset(request, message);
        validateQueueOffset(request, message);
    }

    private void validateProducerGroup(SubmitRequest request, MessageBO message) {
        String groupInProperty = message.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP);
        if (request.getProducerGroup().equals(groupInProperty)) {
            return;
        }

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
            "Transaction message producer group is not equal to request producer group"
        );
    }

    private void validateCommitOffset(SubmitRequest request, MessageBO message) {
        if (message.getCommitOffset() == request.getCommitOffset()) {
            return;
        }

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
            "Transaction message commit offset is not equal to request commit offset"
        );
    }

    private void validateQueueOffset(SubmitRequest request, MessageBO message) {
        if (message.getQueueOffset() == request.getQueueOffset()) {
            return;
        }

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
            "Transaction message queue offset is not equal to request queue offset"
        );
    }

    private void validateCheckTime(SubmitRequest request, MessageBO message) {
        if (request.isFromCheck()) {
            return;
        }

        long checkTime = message.getTransactionCheckTime();
        if (checkTime < 0) {
            return;
        }

        long timeout = transactionConfig.getTransactionTimeout();
        long age = System.currentTimeMillis() - message.getBornTimestamp();
        checkTime = Math.max(checkTime * 1000, timeout);

        if (age > checkTime) {
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY,
                "Transaction message timeout"
            );
        }
    }
}
