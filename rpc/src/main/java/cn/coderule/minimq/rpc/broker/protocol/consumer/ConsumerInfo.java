package cn.coderule.minimq.rpc.broker.protocol.consumer;

import cn.coderule.minimq.domain.domain.enums.ConsumeStrategy;
import cn.coderule.minimq.rpc.broker.protocol.enums.ConsumeType;
import cn.coderule.minimq.rpc.broker.protocol.enums.MessageModel;
import cn.coderule.minimq.rpc.broker.protocol.heartbeat.SubscriptionData;
import cn.coderule.minimq.rpc.common.core.channel.ClientChannelInfo;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerInfo implements Serializable {
    private String groupName;
    private MessageModel messageModel;
    private ConsumeType consumeType;
    private ConsumeStrategy consumeStrategy;
    @Builder.Default
    private Set<SubscriptionData> subscriptionSet = new TreeSet<>();
    private ClientChannelInfo channelInfo;
    private boolean enableChangeNotification;
    @Builder.Default
    private boolean enableSubscriptionModification = false;
    @Builder.Default
    private boolean enableSubscription = true;
}
