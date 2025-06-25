package cn.coderule.minimq.domain.domain.model.consumer.receipt;

import cn.coderule.common.util.lang.bean.BeanUtil;
import com.google.common.base.Objects;

public class HandleKey {
    private final String originalHandle;
    private final String broker;
    private final int queueId;
    private final long offset;

    public HandleKey(String handle) {
        this(ReceiptHandle.decode(handle));
    }

    public HandleKey(ReceiptHandle receiptHandle) {
        this.originalHandle = receiptHandle.getReceiptHandle();
        this.broker = receiptHandle.getBrokerName();
        this.queueId = receiptHandle.getQueueId();
        this.offset = receiptHandle.getOffset();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HandleKey key = (HandleKey) o;
        return queueId == key.queueId && offset == key.offset && Objects.equal(broker, key.broker);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(broker, queueId, offset);
    }

    @Override
    public String toString() {
        return BeanUtil.toStringBuilder(this)
            .append("originalHandle", originalHandle)
            .append("broker", broker)
            .append("queueId", queueId)
            .append("offset", offset)
            .toString();
    }

    public String getOriginalHandle() {
        return originalHandle;
    }

    public String getBroker() {
        return broker;
    }

    public int getQueueId() {
        return queueId;
    }

    public long getOffset() {
        return offset;
    }
}
