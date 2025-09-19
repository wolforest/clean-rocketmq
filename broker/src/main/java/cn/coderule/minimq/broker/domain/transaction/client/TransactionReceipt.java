package cn.coderule.minimq.broker.domain.transaction.client;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public class TransactionReceipt implements Comparable<TransactionReceipt> {
    private final String brokerName;
    private final String topic;
    private final long tranStateTableOffset;
    private final long commitLogOffset;
    private final String transactionId;
    private final long checkTimestamp;
    private final long expireMs;

    public TransactionReceipt(String brokerName, String topic, long tranStateTableOffset, long commitLogOffset, String transactionId,
        long checkTimestamp, long expireMs) {
        this.brokerName = brokerName;
        this.topic = topic;
        this.tranStateTableOffset = tranStateTableOffset;
        this.commitLogOffset = commitLogOffset;
        this.transactionId = transactionId;
        this.checkTimestamp = checkTimestamp;
        this.expireMs = expireMs;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getTopic() {
        return topic;
    }

    public long getTranStateTableOffset() {
        return tranStateTableOffset;
    }

    public long getCommitLogOffset() {
        return commitLogOffset;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getCheckTimestamp() {
        return checkTimestamp;
    }

    public long getExpireMs() {
        return expireMs;
    }

    public long getExpireTime() {
        return checkTimestamp + expireMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionReceipt data = (TransactionReceipt) o;
        return tranStateTableOffset == data.tranStateTableOffset && commitLogOffset == data.commitLogOffset &&
            getExpireTime() == data.getExpireTime() && Objects.equal(brokerName, data.brokerName) &&
            Objects.equal(transactionId, data.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(brokerName, transactionId, tranStateTableOffset, commitLogOffset, getExpireTime());
    }

    @Override
    public int compareTo(TransactionReceipt o) {
        return ComparisonChain.start()
            .compare(getExpireTime(), o.getExpireTime())
            .compare(brokerName, o.brokerName)
            .compare(commitLogOffset, o.commitLogOffset)
            .compare(tranStateTableOffset, o.tranStateTableOffset)
            .compare(transactionId, o.transactionId)
            .result();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("brokerName", brokerName)
            .add("tranStateTableOffset", tranStateTableOffset)
            .add("commitLogOffset", commitLogOffset)
            .add("transactionId", transactionId)
            .add("checkTimestamp", checkTimestamp)
            .add("expireMs", expireMs)
            .toString();
    }
}
