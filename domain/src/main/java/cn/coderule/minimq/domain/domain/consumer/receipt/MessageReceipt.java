
package cn.coderule.minimq.domain.domain.consumer.receipt;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.netty.channel.Channel;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceipt implements Serializable {
    private RequestContext requestContext;
    private Channel channel;

    private String group;
    private String topic;
    private int queueId;
    private String messageId;
    private long queueOffset;
    // this original means where the MessageReceiptHandle property data from
    private String originalReceiptHandleStr;
    private ReceiptHandle originalReceiptHandle;
    private int reconsumeTimes;

    @Builder.Default
    private AtomicInteger renewRetryTimes = new AtomicInteger(0);
    @Builder.Default
    private AtomicInteger renewTimes = new AtomicInteger(0);
    private long consumeTimestamp;
    private volatile String receiptHandleStr;

    public MessageReceipt(String group, String topic, int queueId, String receiptHandleStr, String messageId,
        long queueOffset, int reconsumeTimes) {
        this.originalReceiptHandle = ReceiptHandle.decode(receiptHandleStr);
        this.group = group;
        this.topic = topic;
        this.queueId = queueId;
        this.receiptHandleStr = receiptHandleStr;
        this.originalReceiptHandleStr = receiptHandleStr;
        this.messageId = messageId;
        this.queueOffset = queueOffset;
        this.reconsumeTimes = reconsumeTimes;
        this.consumeTimestamp = originalReceiptHandle.getRetrieveTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageReceipt handle = (MessageReceipt) o;
        return queueId == handle.queueId && queueOffset == handle.queueOffset && consumeTimestamp == handle.consumeTimestamp
            && reconsumeTimes == handle.reconsumeTimes
            && Objects.equal(group, handle.group) && Objects.equal(topic, handle.topic)
            && Objects.equal(messageId, handle.messageId) && Objects.equal(originalReceiptHandleStr, handle.originalReceiptHandleStr)
            && Objects.equal(receiptHandleStr, handle.receiptHandleStr);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, topic, queueId, messageId, queueOffset, originalReceiptHandleStr, consumeTimestamp,
            reconsumeTimes, receiptHandleStr);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("group", group)
            .add("topic", topic)
            .add("queueId", queueId)
            .add("messageId", messageId)
            .add("queueOffset", queueOffset)
            .add("originalReceiptHandleStr", originalReceiptHandleStr)
            .add("reconsumeTimes", reconsumeTimes)
            .add("renewRetryTimes", renewRetryTimes)
            .add("firstConsumeTimestamp", consumeTimestamp)
            .add("receiptHandleStr", receiptHandleStr)
            .toString();
    }

    public String getGroup() {
        return group;
    }

    public String getTopic() {
        return topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public String getReceiptHandleStr() {
        return receiptHandleStr;
    }

    public String getOriginalReceiptHandleStr() {
        return originalReceiptHandleStr;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getQueueOffset() {
        return queueOffset;
    }

    public int getReconsumeTimes() {
        return reconsumeTimes;
    }

    public long getConsumeTimestamp() {
        return consumeTimestamp;
    }

    public void updateReceiptHandle(String receiptHandleStr) {
        this.receiptHandleStr = receiptHandleStr;
    }

    public int incrementAndGetRenewRetryTimes() {
        return this.renewRetryTimes.incrementAndGet();
    }

    public int incrementRenewTimes() {
        return this.renewTimes.incrementAndGet();
    }

    public int getRenewTimes() {
        return this.renewTimes.get();
    }

    public void resetRenewRetryTimes() {
        this.renewRetryTimes.set(0);
    }

    public int getRenewRetryTimes() {
        return this.renewRetryTimes.get();
    }

    public ReceiptHandle getOriginalReceiptHandle() {
        return originalReceiptHandle;
    }
}
