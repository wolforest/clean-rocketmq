package com.wolfmq.broker.domain.consumer.revive;

import com.wolfmq.common.config.business.MessageConfig;
import com.wolfmq.common.config.business.TopicConfig;
import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;
import com.wolfmq.common.domain.offset.ConsumeOffsetService;
import com.wolfmq.common.domain.meta.service.SubscriptionService;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.domain.messagequeue.MQService;
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
    private TopicService topicService;
    private SubscriptionService subscriptionService;
    private ConsumeOffsetService consumeOffsetService;
}
