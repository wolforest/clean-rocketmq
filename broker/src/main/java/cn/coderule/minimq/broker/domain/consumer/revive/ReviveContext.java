package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.business.TopicConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
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
    private ConsumeQueueGateway consumeQueueGateway;;
    private TopicService topicService;
    private SubscriptionService subscriptionService;
    private ConsumeOffsetService consumeOffsetService;
}
