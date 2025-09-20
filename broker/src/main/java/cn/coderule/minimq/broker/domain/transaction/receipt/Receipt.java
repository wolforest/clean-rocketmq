package cn.coderule.minimq.broker.domain.transaction.receipt;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt implements Comparable<Receipt>, Serializable {
    private String brokerName;
    private String topic;
    private String producerGroup;
    private String transactionId;

    private long tranStateTableOffset;
    private long commitLogOffset;
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
        return tranStateTableOffset == data.tranStateTableOffset && commitLogOffset == data.commitLogOffset &&
            getExpireTime() == data.getExpireTime() && Objects.equal(brokerName, data.brokerName) &&
            Objects.equal(transactionId, data.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(brokerName, transactionId, tranStateTableOffset, commitLogOffset, getExpireTime());
    }

    @Override
    public int compareTo(Receipt o) {
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
