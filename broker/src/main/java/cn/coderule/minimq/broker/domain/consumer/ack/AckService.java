package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegistry;
import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandler;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {

    private final AckValidator ackValidator;
    private final ReceiptHandler receiptHandler;

    private final MQFacade mqStore;
    private final ConsumerRegistry consumerRegistry;

    public AckService(
        MQFacade mqStore,
        ConsumerRegistry consumerRegistry,
        ReceiptHandler receiptHandler,
        AckValidator ackValidator
    ) {

        this.mqStore = mqStore;
        this.consumerRegistry = consumerRegistry;

        this.receiptHandler = receiptHandler;
        this.ackValidator = ackValidator;
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        removeReceipt(request);

        AckMessage ackMessage = AckConverter.toAckMessage(request);
        ackValidator.validate(ackMessage);

        mqStore.ack(ackMessage);

        AckResult result = AckResult.success();
        return CompletableFuture.completedFuture(result);
    }

    private void removeReceipt(AckRequest request) {
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

    private MessageReceipt buildRequestReceipt(AckRequest request) {
        ClientChannelInfo channelInfo = consumerRegistry.findChannel(
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
