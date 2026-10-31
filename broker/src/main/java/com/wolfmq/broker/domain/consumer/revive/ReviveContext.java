package com.wolfmq.broker.domain.consumer.revive;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.SubscriptionService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.domain.mq.MQService;
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
    private ConsumeQueueProxy consumeQueueProxy;;
    private TopicService topicService;
    private SubscriptionService subscriptionService;
    private ConsumeOffsetService consumeOffsetService;
}
