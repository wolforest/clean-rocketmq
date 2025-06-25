package cn.coderule.minimq.domain.domain.model.consumer;

import cn.coderule.minimq.domain.domain.model.consumer.running.ConsumerGroupInfo;
import cn.coderule.minimq.domain.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.core.enums.consume.ConsumeType;
import cn.coderule.minimq.domain.domain.core.enums.message.MessageModel;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
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
    @Builder.Default
    private boolean enableSubscriptionModification = false;

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
