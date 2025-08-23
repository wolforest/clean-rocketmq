package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.domain.core.EventListener;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.domain.consumer.revive.RenewEvent;
import java.util.concurrent.CompletableFuture;

public class RenewListener implements EventListener<RenewEvent> {
    private final InvisibleService invisibleService;

    public RenewListener(InvisibleService invisibleService) {
        this.invisibleService = invisibleService;
    }

    @Override
    public void fire(RenewEvent event) {
        RequestContext context = createContext(event);
        InvisibleRequest request = createInvisibleRequest(event, context);

        invisibleService.changeInvisible(request)
            .whenComplete((ackResult, throwable) -> {
                if (throwable != null) {
                    event.getFuture().completeExceptionally(throwable);
                    return;
                }

                event.getFuture().complete(ackResult);
            });
    }

    public void fireClearEvent(
        long renewTime,
        MessageReceipt receipt,
        ReceiptHandleGroupKey key
    ) {
        fireClearEvent(renewTime, receipt, key, new CompletableFuture<>());
    }

    public void fireClearEvent(
        long renewTime,
        MessageReceipt receipt,
        ReceiptHandleGroupKey key,
        CompletableFuture<AckResult> future
    ) {
        RenewEvent event = RenewEvent.builder()
            .key(key)
            .messageReceipt(receipt)
            .future(new CompletableFuture<>())
            .eventType(RenewEvent.EventType.CLEAR_GROUP)
            .renewTime(renewTime)
            .build();

        this.fire(event);
    }

    public void fireRenewEvent(
        long renewTime,
        MessageReceipt receipt,
        ReceiptHandleGroupKey key,
        CompletableFuture<AckResult> future
    ) {
        RenewEvent event = RenewEvent.builder()
            .key(key)
            .future(future)
            .renewTime(renewTime)
            .messageReceipt(receipt)
            .eventType(RenewEvent.EventType.RENEW)
            .build();

        this.fire(event);
    }

    public void fireStopRenewEvent(
        long renewTime,
        MessageReceipt receipt,
        ReceiptHandleGroupKey key,
        CompletableFuture<AckResult> future
    ) {
        RenewEvent event = RenewEvent.builder()
            .key(key)
            .future(future)
            .renewTime(renewTime)
            .messageReceipt(receipt)
            .eventType(RenewEvent.EventType.STOP_RENEW)
            .build();

        this.fire(event);
    }

    private InvisibleRequest createInvisibleRequest(RenewEvent event, RequestContext context) {
        MessageReceipt receipt = event.getMessageReceiptHandle();
        ReceiptHandle handle = ReceiptHandle.decode(receipt.getReceiptHandleStr());

        return InvisibleRequest.builder()
            .requestContext(context)
            .receiptHandle(handle)
            .messageId(receipt.getMessageId())
            .topicName(receipt.getTopic())
            .groupName(receipt.getGroup())
            .invisibleTime(event.getRenewTime())
            .build();
    }

    private RequestContext createContext(RenewEvent event) {
        RequestContext context = RequestContext.create(
            event.getEventType().name()
        );
        context.setChannel(event.getKey().getChannel());

        return context;
    }
}
