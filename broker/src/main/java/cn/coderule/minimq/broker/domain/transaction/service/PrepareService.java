package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareService {
    private final TransactionConfig transactionConfig;
    private final MQStore mqStore;
    private final MessageFactory messageFactory;
    private final ReceiptRegistry receiptRegistry;

    public PrepareService(TransactionConfig transactionConfig, MessageFactory messageFactory, MQStore mqStore, ReceiptRegistry receiptRegistry) {
        this.transactionConfig = transactionConfig;

        this.mqStore = mqStore;
        this.messageFactory = messageFactory;
        this.receiptRegistry = receiptRegistry;
    }

    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        MessageBO prepareMessage = messageFactory.createPrepareMessage(messageBO);
        EnqueueRequest request = EnqueueRequest.builder()
            .requestContext(context)
            .messageBO(prepareMessage)
            .build();

        return mqStore.enqueueAsync(request)
            .thenApplyAsync(result -> registerReceipt(prepareMessage, result));
    }

    private EnqueueResult registerReceipt(MessageBO prepareMessage, EnqueueResult result) {
        String topic = prepareMessage.getRealTopic();
        Receipt receipt = Receipt.builder()
            .topic(topic)
            .producerGroup(topic)

            .storeGroup(result.getStoreGroup())
            .messageId(result.getMessageId())
            .transactionId(result.getTransactionId())
            .commitOffset(result.getCommitOffset())
            .queueOffset(result.getQueueOffset())

            .checkTimestamp(System.currentTimeMillis())
            .expireMs(transactionConfig.getReceiptExpireTime())
            .build();
        receiptRegistry.register(receipt);

        return result;
    }
}
