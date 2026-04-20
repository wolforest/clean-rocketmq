package cn.coderule.wolfmq.domain.domain.consumer;

import cn.coderule.wolfmq.domain.domain.consumer.running.ConsumerGroupInfo;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeType;
import cn.coderule.wolfmq.domain.core.enums.message.MessageModel;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
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
