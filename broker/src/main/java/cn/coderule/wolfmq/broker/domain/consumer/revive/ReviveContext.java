package cn.coderule.wolfmq.broker.domain.consumer.revive;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.wolfmq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.SubscriptionService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.MQService;
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
    private BrokerConfig brokerConfig;
    private TopicConfig topicConfig;
    private MessageConfig messageConfig;

    private String reviveTopic;
    private RetryService retryService;

    private MQFacade mqFacade;
    private TopicFacade topicFacade;
    private SubscriptionFacade subscriptionFacade;
    private ConsumeOffsetFacade consumeOffsetFacade;

    private MQService mqService;
    private ConsumeQueueManager consumeQueueManager;;
    private TopicService topicService;
    private SubscriptionService subscriptionService;
    private ConsumeOffsetService consumeOffsetService;
}
