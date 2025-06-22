package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviveContext implements Serializable {
    private StoreConfig storeConfig;
    private MessageConfig messageConfig;

    private String reviveTopic;
    private RetryService retryService;

    private MQService mqService;
    private ConsumeQueueGateway consumeQueueGateway;;
    private TopicService topicService;
    private SubscriptionService subscriptionService;
    private ConsumeOffsetService consumeOffsetService;
}
