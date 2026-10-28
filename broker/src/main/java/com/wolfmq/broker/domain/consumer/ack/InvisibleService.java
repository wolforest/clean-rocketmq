package com.wolfmq.broker.domain.consumer.ack;

import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.ClientChannelInfo;
import com.wolfmq.domain.domain.consumer.ack.AckConverter;
import com.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import com.wolfmq.domain.domain.consumer.receipt.MessageReceipt;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import com.wolfmq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;

public class InvisibleService {
    private final BrokerConfig brokerConfig;
    private final MQFacade mqStore;

    private final ConsumerManager consumerManager;
    private final ReceiptHandler receiptHandler;
    private final AckValidator ackValidator;

    public InvisibleService(
        BrokerConfig brokerConfig,
        MQFacade mqStore,
        ConsumerManager consumerManager,
        ReceiptHandler receiptHandler,
        AckValidator ackValidator
    ) {
        this.brokerConfig = brokerConfig;

        this.mqStore = mqStore;
        this.consumerManager = consumerManager;

        this.receiptHandler = receiptHandler;
        this.ackValidator = ackValidator;
    }

    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        removeReceipt(request);

        AckMessage ackMessage = AckConverter.toAckMessage(request);
        ackValidator.validate(ackMessage);

        AckResult result = mqStore.changeInvisible(ackMessage);
        return CompletableFuture.completedFuture(result);
    }

    private void removeReceipt(InvisibleRequest request) {
        MessageReceipt requestReceipt =  buildRequestReceipt(request);
        if (requestReceipt == null) {
            return;
        }

        MessageReceipt receipt = receiptHandler.removeReceipt(requestReceipt);
        if (receipt == null) {
            return;
        }

        request.setReceiptStr(receipt.getReceiptHandleStr());
    }

    private MessageReceipt buildRequestReceipt(InvisibleRequest request) {
        ClientChannelInfo channelInfo = consumerManager.findChannel(
            request.getGroupName(),
            request.getRequestContext().getClientID()
        );
        if (channelInfo == null) {
            return null;
        }

        return MessageReceipt.builder()
            .group(request.getGroupName())
            .messageId(request.getMessageId())
            .receiptHandleStr(request.getReceiptStr())
            .channel(channelInfo.getChannel())
            .build();
    }
}
