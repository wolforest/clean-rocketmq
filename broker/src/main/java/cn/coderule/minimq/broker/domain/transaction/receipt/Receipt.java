package cn.coderule.minimq.broker.domain.transaction.receipt;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * transaction receipt
 * @rocketmq original name: TransactionData
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt implements Comparable<Receipt>, Serializable {
    private String storeGroup;
    private String topic;
    private String producerGroup;
    private String transactionId;
    private String messageId;

    private long queueOffset;
    private long commitOffset;
    private long checkTimestamp;
    private long expireMs;

    public String getKey() {
        return buildKey(producerGroup, transactionId);
    }

    public static String buildKey(String producerGroup, String transactionId) {
        return producerGroup + "@" + transactionId;
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
        Receipt data = (Receipt) o;
        return queueOffset == data.queueOffset && commitOffset == data.commitOffset &&
            getExpireTime() == data.getExpireTime() && Objects.equal(storeGroup, data.storeGroup) &&
            Objects.equal(transactionId, data.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(storeGroup, transactionId, queueOffset, commitOffset, getExpireTime());
    }

    @Override
    public int compareTo(Receipt o) {
        return ComparisonChain.start()
            .compare(getExpireTime(), o.getExpireTime())
            .compare(storeGroup, o.storeGroup)
            .compare(commitOffset, o.commitOffset)
            .compare(queueOffset, o.queueOffset)
            .compare(transactionId, o.transactionId)
            .result();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("brokerName", storeGroup)
            .add("tranStateTableOffset", queueOffset)
            .add("commitLogOffset", commitOffset)
            .add("transactionId", transactionId)
            .add("checkTimestamp", checkTimestamp)
            .add("expireMs", expireMs)
            .toString();
    }
}
