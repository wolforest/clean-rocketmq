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
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import cn.coderule.minimq.domain.domain.transaction.OffsetQueue;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.TransactionUtil;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService {
    private final TransactionConfig transactionConfig;

    private final MQStore mqStore;
    private final CommitBuffer commitBuffer;
    private final MessageFactory messageFactory;
    private final SubmitValidator submitValidator;
    private final BatchCommitService batchCommitService;

    public MessageService(
        BrokerConfig brokerConfig,
        CommitBuffer commitBuffer,
        BatchCommitService batchCommitService,
        MessageFactory messageFactory,
        MQStore mqStore
    ) {
        this.transactionConfig = brokerConfig.getTransactionConfig();

        this.mqStore = mqStore;
        this.commitBuffer = commitBuffer;
        this.messageFactory = messageFactory;
        this.batchCommitService = batchCommitService;
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

    public void deletePrepareMessage(SubmitRequest request, MessageBO messageBO) {
        OffsetQueue offsetQueue = commitBuffer.initOffsetQueue(messageBO.getQueueId());

        boolean status = wakeupBatchCommitService(messageBO, offsetQueue);
        if (status) {
            return;
        }

        MessageQueue operationQueue = commitBuffer.initOperationQueue(messageBO.getQueueId(), request.getStoreGroup());
        MessageBO operationMessage = messageFactory.createOperationMessage(
            offsetQueue, operationQueue, messageBO.getQueueOffset());
        if (operationMessage == null) {
            return;
        }

        enqueueOperationMessage(request, operationMessage);
    }

    private void enqueueOperationMessage(SubmitRequest request, MessageBO operationMessage) {
        EnqueueRequest enqueueRequest = EnqueueRequest.builder()
            .requestContext(request.getRequestContext())
            .storeGroup(request.getStoreGroup())
            .messageBO(operationMessage)
            .build();
        mqStore.enqueue(enqueueRequest);
    }

    private boolean wakeupBatchCommitService(MessageBO messageBO, OffsetQueue offsetQueue) {
        try {
            String offsetKey = TransactionUtil.buildOffsetKey(messageBO.getQueueOffset());
            boolean res = offsetQueue.offer(offsetKey, 100);
            if (!res) {
                batchCommitService.wakeup();
                return false;
            }

            int keyLength = offsetKey.length();
            int totalSize = offsetQueue.addAndGet(keyLength);
            if (totalSize > transactionConfig.getMaxCommitMessageLength()) {
                batchCommitService.wakeup();
            }
            return true;
        } catch (Exception ignore) {
        }

        return false;
    }

    public EnqueueResult enqueueCommitMessage(SubmitRequest request, MessageBO messageBO) {
        EnqueueRequest enqueueRequest = EnqueueRequest.builder()
            .requestContext(request.getRequestContext())
            .storeGroup(request.getStoreGroup())
            .messageBO(messageBO)
            .build();
        return mqStore.enqueue(enqueueRequest);
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
