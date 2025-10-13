package cn.coderule.minimq.broker.domain.transaction.check.service;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.producer.ProducerRegister;
import cn.coderule.minimq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckService implements Lifecycle {
    private final TransactionConfig transactionConfig;
    private final ReceiptRegistry receiptRegistry;

    private final ExecutorService executor;

    private ProducerRegister producerRegister;

    public CheckService(TransactionConfig transactionConfig, ReceiptRegistry receiptRegistry) {
        this.transactionConfig = transactionConfig;
        this.receiptRegistry = receiptRegistry;

        this.executor = initExecutor();
    }

    public void inject(ProducerRegister producerRegister) {
        this.producerRegister = producerRegister;
    }

    public void check(MessageBO prepareMessage) {
        executor.execute(() -> {
            try {
                checkAsync(prepareMessage);
            } catch (Exception e) {
                log.error("check message error", e);
            }
        });
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
    }

    private void checkAsync(MessageBO prepareMessage) {
        MessageBO checkMessage = createCheckMessage(prepareMessage);

        String realTopic = checkMessage.getTopic();
        if (StringUtil.isBlank(realTopic)) {
            log.error("message producer topic is null, message: {}", checkMessage);
            return;
        }

        RelayService relayService = producerRegister.getRelayService(realTopic);
        if (relayService == null) {
            log.error("can't find RelayService by producerGroup: {}", realTopic);
            return;
        }

        registerReceipt(checkMessage);

        TransactionRequest request = TransactionRequest.build(checkMessage);
        relayService.checkTransaction(request);
    }

    private MessageBO createCheckMessage(MessageBO prepareMessage) {
        return MessageBO.builder()
            .topic(prepareMessage.getRealTopic())
            .queueId(prepareMessage.getRealQueueId())

            .body(prepareMessage.getBody())
            .flag(prepareMessage.getFlag())
            .transactionId(prepareMessage.getTransactionId())

            .storeGroup(prepareMessage.getStoreGroup())
            .messageId(prepareMessage.getMessageId())
            .commitOffset(prepareMessage.getCommitOffset())
            .queueOffset(prepareMessage.getQueueOffset())
            .properties(prepareMessage.getProperties())

            .build();
    }

    private void registerReceipt(MessageBO checkMessage) {
        Receipt receipt = Receipt.builder()
            .topic(checkMessage.getTopic())
            .producerGroup(checkMessage.getProducerGroup())

            .storeGroup(checkMessage.getStoreGroup())
            .messageId(checkMessage.getMessageId())
            .transactionId(checkMessage.getTransactionId())
            .commitOffset(checkMessage.getCommitOffset())
            .queueOffset(checkMessage.getQueueOffset())

            .checkTimestamp(System.currentTimeMillis())
            .expireMs(transactionConfig.getReceiptExpireTime())
            .build();
        receiptRegistry.register(receipt);
    }

    private ExecutorService initExecutor() {
        return ThreadUtil.newThreadPoolExecutor(
            transactionConfig.getCheckThreadNum(),
            transactionConfig.getMaxCheckThreadNum(),
            transactionConfig.getKeepAliveTime(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(transactionConfig.getCheckQueueCapacity()),
            new DefaultThreadFactory("TransactionCheckThread_"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
