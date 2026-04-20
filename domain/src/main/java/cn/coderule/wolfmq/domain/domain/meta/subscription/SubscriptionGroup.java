
package cn.coderule.wolfmq.domain.domain.meta.subscription;

import cn.coderule.wolfmq.domain.core.constant.MQConstants;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionGroup implements Serializable {

    private String groupName;

    private boolean consumeEnable = true;
    private boolean consumeFromMinEnable = true;
    private boolean consumeBroadcastEnable = true;
    private boolean consumeMessageOrderly = false;

    private int retryQueueNums = 1;

    private int retryMaxTimes = 16;
    private GroupRetryPolicy groupRetryPolicy = new GroupRetryPolicy();

    private long brokerId = MQConstants.MASTER_ID;

    private long whichBrokerWhenConsumeSlowly = 1;

    private boolean notifyConsumerIdsChangedEnable = true;

    private int groupSysFlag = 0;

    // Only valid for push consumer
    private int consumeTimeoutMinute = 15;

    private Set<SimpleSubscriptionData> subscriptionDataSet;

    private Map<String, String> attributes = new HashMap<>();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.hashCode(brokerId);
        result = prime * result + (consumeBroadcastEnable ? 1231 : 1237);
        result = prime * result + (consumeEnable ? 1231 : 1237);
        result = prime * result + (consumeFromMinEnable ? 1231 : 1237);
        result = prime * result + (notifyConsumerIdsChangedEnable ? 1231 : 1237);
        result = prime * result + (consumeMessageOrderly ? 1231 : 1237);
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result + retryMaxTimes;
        result = prime * result + retryQueueNums;
        result =
            prime * result + Long.hashCode(whichBrokerWhenConsumeSlowly);
        result = prime * result + groupSysFlag;
        result = prime * result + consumeTimeoutMinute;
        result = prime * result + subscriptionDataSet.hashCode();
        result = prime * result + attributes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionGroup other = (SubscriptionGroup) obj;
        return new EqualsBuilder()
            .append(groupName, other.groupName)
            .append(consumeEnable, other.consumeEnable)
            .append(consumeFromMinEnable, other.consumeFromMinEnable)
            .append(consumeBroadcastEnable, other.consumeBroadcastEnable)
            .append(consumeMessageOrderly, other.consumeMessageOrderly)
            .append(retryQueueNums, other.retryQueueNums)
            .append(retryMaxTimes, other.retryMaxTimes)
            .append(whichBrokerWhenConsumeSlowly, other.whichBrokerWhenConsumeSlowly)
            .append(notifyConsumerIdsChangedEnable, other.notifyConsumerIdsChangedEnable)
            .append(groupSysFlag, other.groupSysFlag)
            .append(consumeTimeoutMinute, other.consumeTimeoutMinute)
            .append(subscriptionDataSet, other.subscriptionDataSet)
            .append(attributes, other.attributes)
            .isEquals();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("groupName", groupName)
            .add("consumeEnable", consumeEnable)
            .add("consumeFromMinEnable", consumeFromMinEnable)
            .add("consumeBroadcastEnable", consumeBroadcastEnable)
            .add("consumeMessageOrderly", consumeMessageOrderly)
            .add("retryQueueNums", retryQueueNums)
            .add("retryMaxTimes", retryMaxTimes)
            .add("groupRetryPolicy", groupRetryPolicy)
            .add("brokerId", brokerId)
            .add("whichBrokerWhenConsumeSlowly", whichBrokerWhenConsumeSlowly)
            .add("notifyConsumerIdsChangedEnable", notifyConsumerIdsChangedEnable)
            .add("groupSysFlag", groupSysFlag)
            .add("consumeTimeoutMinute", consumeTimeoutMinute)
            .add("subscriptionDataSet", subscriptionDataSet)
            .add("attributes", attributes)
            .toString();
    }
}
