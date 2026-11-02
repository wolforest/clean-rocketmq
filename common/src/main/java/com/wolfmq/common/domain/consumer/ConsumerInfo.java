package com.wolfmq.common.domain.consumer;

import com.wolfmq.common.domain.consumer.running.ConsumerGroupInfo;
import com.wolfmq.common.domain.consumer.consume.ConsumeStrategy;
import com.wolfmq.common.domain.consumer.consume.ConsumeType;
import com.wolfmq.common.domain.message.enums.MessageModel;
import com.wolfmq.common.server.ClientChannelInfo;
import com.wolfmq.common.server.RequestContext;
import com.wolfmq.common.server.heartbeat.SubscriptionData;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerInfo implements Serializable {
    private RequestContext requestContext;

    private String groupName;
    private MessageModel messageModel;
    private ConsumeType consumeType;
    private ConsumeStrategy consumeStrategy;
    @Builder.Default
    private Set<SubscriptionData> subscriptionSet = new TreeSet<>();
    private ClientChannelInfo channelInfo;
    @Builder.Default
    private boolean enableNotification = false;
    /**
     * enable modification of subscription
     */
    @Builder.Default
    private boolean enableModification = false;

    public Set<String> getTopicSet() {
        return subscriptionSet.stream()
            .map(SubscriptionData::getTopic)
            .collect(Collectors.toSet());
    }

    public ConsumerGroupInfo toGroupInfo() {
        return new ConsumerGroupInfo(
            this.groupName,
            this.consumeType,
            this.messageModel,
            this.consumeStrategy
        );
    }
}
