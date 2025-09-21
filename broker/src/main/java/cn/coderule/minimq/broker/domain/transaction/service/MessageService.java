package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService {
    private final TransactionConfig transactionConfig;

    private final MQStore mqStore;
    private final SubmitValidator submitValidator;

    public MessageService(BrokerConfig brokerConfig, MQStore mqStore) {
        this.transactionConfig = brokerConfig.getTransactionConfig();

        this.mqStore = mqStore;
        this.submitValidator = new SubmitValidator(transactionConfig);
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

    public void deletePrepareMessage(MessageBO messageBO) {
    }

    public EnqueueResult enqueueCommitMessage(SubmitRequest request, MessageBO messageBO) {
        return null;
    }

    public MessageBO getMessage(SubmitRequest submitRequest) {
        MessageRequest request = MessageRequest.builder()
                .storeGroup(submitRequest.getStoreGroup())
                .offset(submitRequest.getCommitOffset())
                .size(1)
                .build();

        MessageResult result = mqStore.getMessage(request);

        submitValidator.validate(submitRequest, result.getMessage());
        return result.getMessage();
    }
}
