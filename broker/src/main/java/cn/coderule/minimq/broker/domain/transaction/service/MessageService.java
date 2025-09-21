package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService {
    private final TransactionConfig transactionConfig;
    private final MQStore mqStore;

    public MessageService(BrokerConfig brokerConfig, MQStore mqStore) {
        this.transactionConfig = brokerConfig.getTransactionConfig();
        this.mqStore = mqStore;
    }

    public long getConsumeOffset(MessageQueue mq) {
        return 0;
    }

    public void updateConsumeOffset(MessageQueue mq, long offset) {

    }

    public Set<MessageQueue> getMessageQueues(String storeGroup, String topic) {
        Set<MessageQueue> result = new HashSet<>();

        if (!result.isEmpty()) {
            return result;
        }

        log.warn("no prepare message queue: storeGroup={}, topic={}", storeGroup, topic);
        return result;
    }

    public DequeueResult getMessage(MessageQueue mq, int num) {
        return null;
    }

    public MessageBO getMessage(String storeGroup, long commitOffset) {
        MessageRequest request = MessageRequest.builder()
                .storeGroup(storeGroup)
                .offset(commitOffset)
                .size(1)
                .build();

        MessageResult result = mqStore.getMessage(request);
        return result.getMessage();
    }

    public void validateMessage(SubmitRequest request, MessageBO message) {
        if (message == null) {
            throw new InvalidRequestException(
                InvalidCode.MESSAGE_NOT_FOUND,
                "Can't find transaction message, while commit"
            );
        }

        if (request.isFromCheck()) {
            return;
        }

        validateCheckTime(message);
    }

    private void validateCheckTime(MessageBO message) {
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
