
package cn.coderule.minimq.domain.domain.model.consumer;

import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckResult;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.ReceiptHandleGroupKey;
import java.util.concurrent.CompletableFuture;

public class RenewEvent {
    protected ReceiptHandleGroupKey key;
    protected MessageReceipt messageReceipt;
    protected long renewTime;
    protected EventType eventType;
    protected CompletableFuture<AckResult> future;

    public enum EventType {
        RENEW,
        STOP_RENEW,
        CLEAR_GROUP
    }

    public RenewEvent(ReceiptHandleGroupKey key, MessageReceipt messageReceipt, long renewTime,
        EventType eventType, CompletableFuture<AckResult> future) {
        this.key = key;
        this.messageReceipt = messageReceipt;
        this.renewTime = renewTime;
        this.eventType = eventType;
        this.future = future;
    }

    public ReceiptHandleGroupKey getKey() {
        return key;
    }

    public MessageReceipt getMessageReceiptHandle() {
        return messageReceipt;
    }

    public long getRenewTime() {
        return renewTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public CompletableFuture<AckResult> getFuture() {
        return future;
    }
}
